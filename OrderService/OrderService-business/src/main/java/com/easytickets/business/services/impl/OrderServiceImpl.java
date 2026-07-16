package com.easytickets.business.services.impl;

import com.easytickets.business.dto.EventOrderStatsDto;
import com.easytickets.business.dto.OrderDto;
import com.easytickets.business.dto.OrderStatus;
import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.dto.event.PaymentSuccessEvent;
import com.easytickets.business.dto.event.TicketReservedEvent;
import com.easytickets.business.exception.OrderAccessDeniedException;
import com.easytickets.business.exception.OrderNotFoundException;
import com.easytickets.business.repo.OrderRepo;
import com.easytickets.business.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;

    @Override
    public OrderDto createFromReservation(TicketReservedEvent event) {
        return orderRepo.findByReservationId(event.getReservationId())
                .map(existing -> {
                    log.info("Reservation already processed, skipping duplicate. reservationId={}, orderId={}",
                            event.getReservationId(), existing.getId());
                    return existing;
                })
                .orElseGet(() -> {
                    OrderDto order = OrderDto.builder()
                            .reservationId(event.getReservationId())
                            .userId(event.getUserId())
                            .eventId(event.getEventId())
                            .ticketTypeId(event.getTicketTypeId())
                            .quantity(event.getQuantity())
                            .unitPrice(event.getUnitPrice())
                            .totalAmount(event.getUnitPrice().multiply(BigDecimal.valueOf(event.getQuantity())))
                            .status(OrderStatus.PENDING_PAYMENT)
                            .build();
                    OrderDto saved = orderRepo.save(order);
                    log.info("Order created. orderId={}, reservationId={}, userId={}, eventId={}",
                            saved.getId(), saved.getReservationId(), saved.getUserId(), saved.getEventId());
                    return saved;
                });
    }

    @Override
    public OrderDto getOrder(String orderId, String callerUserId, boolean isAdmin) {
        OrderDto order = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!isAdmin && !order.getUserId().equals(callerUserId)) {
            throw new OrderAccessDeniedException("You do not own this order: " + orderId);
        }
        return order;
    }

    @Override
    public void markPaid(PaymentSuccessEvent event) {
        orderRepo.findById(event.getOrderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.PAID) {
                log.info("Order already marked PAID, skipping duplicate. orderId={}", event.getOrderId());
                return;
            }
            OrderDto updated = order.toBuilder()
                    .status(OrderStatus.PAID)
                    .paymentId(event.getPaymentId())
                    .build();
            orderRepo.save(updated);
            log.info("Order marked PAID. orderId={}, paymentId={}", event.getOrderId(), event.getPaymentId());
        }, () -> log.error("Received payment-success for unknown order. orderId={}", event.getOrderId()));
    }

    @Override
    public void markCancelled(PaymentFailedEvent event) {
        orderRepo.findById(event.getOrderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                log.info("Order already marked CANCELLED, skipping duplicate. orderId={}", event.getOrderId());
                return;
            }
            OrderDto updated = order.toBuilder()
                    .status(OrderStatus.CANCELLED)
                    .build();
            orderRepo.save(updated);
            log.info("Order marked CANCELLED. orderId={}, reason={}", event.getOrderId(), event.getReason());
        }, () -> log.error("Received payment-failed for unknown order. orderId={}", event.getOrderId()));
    }

    @Override
    public List<OrderDto> getMyTickets(String userId) {
        return orderRepo.findByUserId(userId);
    }

    @Override
    public List<EventOrderStatsDto> getStatsByEventIds(List<String> eventIds) {
        if (eventIds.isEmpty()) {
            return List.of();
        }
        return orderRepo.sumPaidStatsByEventIds(eventIds);
    }
}
