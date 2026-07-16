package com.easytickets.business.services;

import com.easytickets.business.dto.EventOrderStatsDto;
import com.easytickets.business.dto.OrderDto;
import com.easytickets.business.dto.event.PaymentFailedEvent;
import com.easytickets.business.dto.event.PaymentSuccessEvent;
import com.easytickets.business.dto.event.TicketReservedEvent;

import java.util.List;

public interface OrderService {

    /**
     * Creates the PENDING_PAYMENT order for a Kafka {@code ticket-reserved} message.
     * Idempotent by {@code reservationId} – returns the existing order unchanged if
     * this reservation was already processed (at-least-once delivery safe).
     */
    OrderDto createFromReservation(TicketReservedEvent event);

    /**
     * Fetches an order for display. Buyers may only see their own orders; admins may
     * see any order.
     */
    OrderDto getOrder(String orderId, String callerUserId, boolean isAdmin);

    /**
     * Marks an order PAID for a Kafka {@code payment-success} message. Idempotent by
     * {@code orderId} – a redelivered message for an already-PAID order is a no-op.
     */
    void markPaid(PaymentSuccessEvent event);

    /**
     * Marks an order CANCELLED for a Kafka {@code payment-failed} message. Idempotent
     * by {@code orderId} – a redelivered message for an already-CANCELLED order is a
     * no-op.
     */
    void markCancelled(PaymentFailedEvent event);

    /**
     * Buyer's own ticket purchase history, used by User Service's aggregated
     * {@code /me/ticket-history} endpoint.
     */
    List<OrderDto> getMyTickets(String userId);

    /**
     * Ticket-sold count and revenue for PAID orders, grouped by event. Called by
     * Event Service to build the organizer statistics view.
     */
    List<EventOrderStatsDto> getStatsByEventIds(List<String> eventIds);
}
