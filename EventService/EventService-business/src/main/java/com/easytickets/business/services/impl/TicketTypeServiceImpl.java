package com.easytickets.business.services.impl;

import com.easytickets.business.dto.CreateTicketTypeRequest;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventStatus;
import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.dto.UpdateTicketTypeRequest;
import com.easytickets.business.exception.EventAccessDeniedException;
import com.easytickets.business.exception.EventAlreadyPublishedException;
import com.easytickets.business.exception.EventNotFoundException;
import com.easytickets.business.exception.TicketTypeNotFoundException;
import com.easytickets.business.repo.EventRepo;
import com.easytickets.business.repo.TicketTypeRepo;
import com.easytickets.business.services.TicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepo ticketTypeRepo;
    private final EventRepo eventRepo;

    @Override
    @CacheEvict(cacheNames = "ticket-types", key = "#eventId")
    public TicketTypeDto createTicketType(String eventId, CreateTicketTypeRequest request, String organizerId) {
        getOwnedDraftEvent(eventId, organizerId);

        TicketTypeDto ticketType = TicketTypeDto.builder()
                .eventId(eventId)
                .name(request.getName())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .build();

        TicketTypeDto saved = ticketTypeRepo.save(ticketType);
        log.info("Ticket type created. eventId={}, ticketTypeId={}, organizerId={}", eventId, saved.getId(), organizerId);
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = "ticket-types", key = "#eventId")
    public TicketTypeDto updateTicketType(String eventId, String ticketTypeId, UpdateTicketTypeRequest request, String organizerId) {
        getOwnedDraftEvent(eventId, organizerId);

        TicketTypeDto existing = ticketTypeRepo.findById(ticketTypeId)
                .filter(ticketType -> ticketType.getEventId().equals(eventId))
                .orElseThrow(() -> new TicketTypeNotFoundException("Ticket type not found: " + ticketTypeId));

        TicketTypeDto updated = existing.toBuilder()
                .name(request.getName())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .build();

        TicketTypeDto saved = ticketTypeRepo.save(updated);
        log.info("Ticket type updated. eventId={}, ticketTypeId={}, organizerId={}", eventId, ticketTypeId, organizerId);
        return saved;
    }

    @Override
    @Cacheable(cacheNames = "ticket-types", key = "#eventId")
    public List<TicketTypeDto> listTicketTypes(String eventId) {
        return ticketTypeRepo.findByEventId(eventId);
    }

    private EventDto getOwnedDraftEvent(String eventId, String organizerId) {
        EventDto event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new EventAccessDeniedException("You do not own this event: " + eventId);
        }
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventAlreadyPublishedException("Cannot modify ticket types after the event is published: " + eventId);
        }
        return event;
    }
}
