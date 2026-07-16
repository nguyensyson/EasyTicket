package com.easytickets.business.services;

import com.easytickets.business.dto.PurchaseTicketRequest;
import com.easytickets.business.dto.PurchaseTicketResponse;
import com.easytickets.business.dto.TicketAvailabilityDto;
import com.easytickets.business.dto.event.PaymentFailedEvent;

import java.util.List;

public interface TicketService {

    /**
     * Executes the Redis Lua CHECK & DECREMENT for the requested ticket type/quantity,
     * then publishes {@code ticket-reserved} to Kafka. Returns immediately once the
     * reservation is confirmed – does not wait for Order Service to process it.
     */
    PurchaseTicketResponse purchaseTicket(String eventId, PurchaseTicketRequest request, String userId);

    List<TicketAvailabilityDto> getAvailability(String eventId);

    /**
     * Loads ticket type quantity/price from Event Service into Redis for the given event.
     * Idempotent – no-op if already loaded. Returns the number of ticket types loaded.
     */
    int loadInventory(String eventId);

    /**
     * Consumes {@code payment-failed} (Luồng 6) – releases the reserved stock back to
     * Redis so it can be sold again. Idempotent: no-op if the reservation was already
     * released (or its TTL expired).
     */
    void releaseReservation(PaymentFailedEvent event);
}
