package com.easytickets.business.repo;

import com.easytickets.business.dto.ReservationResult;
import com.easytickets.business.dto.TicketMetaDto;
import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.dto.event.TicketReservedEvent;

import java.util.List;
import java.util.Optional;

/**
 * Port over the Redis-backed ticket inventory – the single source of truth for
 * stock (per the architecture's Atomic Inventory principle). Implemented in
 * {@code TicketService-infratructures} using a Lua script for CHECK & DECREMENT.
 */
public interface TicketInventoryRepo {

    /**
     * Atomically checks and decrements {@code ticket:inventory:{eventId}:{ticketTypeId}}
     * by {@code quantity} in a single Redis operation.
     */
    ReservationResult reserve(String eventId, String ticketTypeId, int quantity);

    /**
     * Compensating action – increments the inventory back by {@code quantity}
     * (used when a reservation cannot be completed, e.g. Kafka publish failure).
     */
    void release(String eventId, String ticketTypeId, int quantity);

    Optional<TicketMetaDto> getMeta(String eventId, String ticketTypeId);

    List<String> getTicketTypeIds(String eventId);

    Optional<Integer> getAvailableQuantity(String eventId, String ticketTypeId);

    boolean isInventoryLoaded(String eventId);

    void loadInventory(String eventId, List<TicketTypeDto> ticketTypes);

    /**
     * Persists {@code ticket:reservation:{reservationId}} for later reconciliation
     * when Order Service / Ticket Service consumes {@code payment-failed} (refund flow).
     */
    void saveReservation(TicketReservedEvent event, long ttlSeconds);

    void deleteReservation(String reservationId);
}
