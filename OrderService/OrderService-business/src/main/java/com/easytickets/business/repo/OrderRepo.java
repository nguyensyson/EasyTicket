package com.easytickets.business.repo;

import com.easytickets.business.dto.EventOrderStatsDto;
import com.easytickets.business.dto.OrderDto;

import java.util.List;
import java.util.Optional;

public interface OrderRepo {

    OrderDto save(OrderDto order);

    Optional<OrderDto> findById(String id);

    Optional<OrderDto> findByReservationId(String reservationId);

    List<OrderDto> findByUserId(String userId);

    /**
     * Aggregates ticket count and revenue for PAID orders, grouped by event, for the
     * given event ids. Used by Event Service to build the organizer statistics view.
     */
    List<EventOrderStatsDto> sumPaidStatsByEventIds(List<String> eventIds);
}
