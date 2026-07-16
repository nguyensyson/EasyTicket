package com.easytickets.business.services.impl;

import com.easytickets.business.client.EventServiceClient;
import com.easytickets.business.dto.PurchaseTicketRequest;
import com.easytickets.business.dto.PurchaseTicketResponse;
import com.easytickets.business.dto.ReservationResult;
import com.easytickets.business.dto.TicketAvailabilityDto;
import com.easytickets.business.dto.TicketMetaDto;
import com.easytickets.business.dto.TicketReservationDto;
import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.dto.event.TicketReservedEvent;
import com.easytickets.business.exception.EventServiceUnavailableException;
import com.easytickets.business.exception.InventoryUnavailableException;
import com.easytickets.business.exception.ReservationFailedException;
import com.easytickets.business.exception.TicketNotFoundException;
import com.easytickets.business.exception.TicketSoldOutException;
import com.easytickets.business.producer.TicketReservedEventPublisher;
import com.easytickets.business.repo.TicketInventoryRepo;
import com.easytickets.business.services.TicketService;
import com.easytickets.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    // Payment timeout (2 min, per PaymentService) + buffer for reconciliation.
    private static final long RESERVATION_TTL_SECONDS = 3 * 60;

    private final TicketInventoryRepo ticketInventoryRepo;
    private final TicketReservedEventPublisher ticketReservedEventPublisher;
    private final EventServiceClient eventServiceClient;

    @Override
    public PurchaseTicketResponse purchaseTicket(String eventId, PurchaseTicketRequest request, String userId) {
        String ticketTypeId = request.getTicketTypeId();
        int quantity = request.getQuantity();

        TicketMetaDto meta = ticketInventoryRepo.getMeta(eventId, ticketTypeId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket type not found or inventory not loaded: " + ticketTypeId));

        ReservationResult result = safeReserve(eventId, ticketTypeId, quantity);
        switch (result.getStatus()) {
            case NOT_FOUND -> throw new TicketNotFoundException("Ticket type not found or inventory not loaded: " + ticketTypeId);
            case SOLD_OUT -> {
                log.info("Ticket sold out. eventId={}, ticketTypeId={}, quantity={}, userId={}", eventId, ticketTypeId, quantity, userId);
                throw new TicketSoldOutException("Het ve");
            }
            default -> log.debug("Inventory decremented. eventId={}, ticketTypeId={}, remaining={}", eventId, ticketTypeId, result.getRemaining());
        }

        String reservationId = UUID.randomUUID().toString();
        TicketReservedEvent event = TicketReservedEvent.builder()
                .reservationId(reservationId)
                .userId(userId)
                .eventId(eventId)
                .ticketTypeId(ticketTypeId)
                .quantity(quantity)
                .unitPrice(meta.getPrice())
                .build();

        try {
            ticketInventoryRepo.saveReservation(event, RESERVATION_TTL_SECONDS);
            ticketReservedEventPublisher.publish(event);
        } catch (Exception ex) {
            log.error("Failed to publish ticket-reserved event, releasing reserved stock. eventId={}, ticketTypeId={}, reservationId={}, userId={}",
                    eventId, ticketTypeId, reservationId, userId, ex);
            ticketInventoryRepo.release(eventId, ticketTypeId, quantity);
            ticketInventoryRepo.deleteReservation(reservationId);
            throw new ReservationFailedException("Unable to reserve ticket, please try again");
        }

        log.info("Ticket reserved. eventId={}, ticketTypeId={}, quantity={}, userId={}, reservationId={}",
                eventId, ticketTypeId, quantity, userId, reservationId);
        return PurchaseTicketResponse.builder().reservationId(reservationId).build();
    }

    @Override
    public List<TicketAvailabilityDto> getAvailability(String eventId) {
        List<String> ticketTypeIds = ticketInventoryRepo.getTicketTypeIds(eventId);
        List<TicketAvailabilityDto> availability = new ArrayList<>();
        for (String ticketTypeId : ticketTypeIds) {
            TicketMetaDto meta = ticketInventoryRepo.getMeta(eventId, ticketTypeId).orElse(null);
            int available = ticketInventoryRepo.getAvailableQuantity(eventId, ticketTypeId).orElse(0);
            availability.add(TicketAvailabilityDto.builder()
                    .ticketTypeId(ticketTypeId)
                    .name(meta != null ? meta.getName() : null)
                    .price(meta != null ? meta.getPrice() : null)
                    .availableQuantity(available)
                    .build());
        }
        return availability;
    }

    @Override
    public int loadInventory(String eventId) {
        if (ticketInventoryRepo.isInventoryLoaded(eventId)) {
            log.info("Inventory already loaded, skip. eventId={}", eventId);
            return 0;
        }

        List<TicketTypeDto> ticketTypes;
        try {
            ApiResponse<List<TicketTypeDto>> response = eventServiceClient.getTicketTypes(eventId);
            ticketTypes = response != null && response.getData() != null ? response.getData() : List.of();
        } catch (Exception ex) {
            log.error("Failed to fetch ticket types from Event Service. eventId={}", eventId, ex);
            throw new EventServiceUnavailableException("Event Service unavailable while loading inventory");
        }

        ticketInventoryRepo.loadInventory(eventId, ticketTypes);
        log.info("Inventory loaded. eventId={}, ticketTypeCount={}", eventId, ticketTypes.size());
        return ticketTypes.size();
    }

    @Override
    public void releaseReservation(PaymentFailedEvent event) {
        String reservationId = event.getReservationId();
        Optional<TicketReservationDto> reservation = ticketInventoryRepo.getReservation(reservationId);
        if (reservation.isEmpty()) {
            log.info("Reservation already released or expired, skip. reservationId={}, orderId={}",
                    reservationId, event.getOrderId());
            return;
        }

        TicketReservationDto data = reservation.get();
        ticketInventoryRepo.release(data.getEventId(), data.getTicketTypeId(), data.getQuantity());
        ticketInventoryRepo.deleteReservation(reservationId);

        log.info("Ticket released back to inventory. reservationId={}, orderId={}, eventId={}, ticketTypeId={}, quantity={}, reason={}",
                reservationId, event.getOrderId(), data.getEventId(), data.getTicketTypeId(), data.getQuantity(), event.getReason());
    }

    private ReservationResult safeReserve(String eventId, String ticketTypeId, int quantity) {
        try {
            return ticketInventoryRepo.reserve(eventId, ticketTypeId, quantity);
        } catch (RuntimeException ex) {
            log.error("Redis inventory operation failed. eventId={}, ticketTypeId={}", eventId, ticketTypeId, ex);
            throw new InventoryUnavailableException("Ticket inventory temporarily unavailable");
        }
    }
}
