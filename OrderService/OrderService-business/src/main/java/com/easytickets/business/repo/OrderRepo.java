package com.easytickets.business.repo;

import com.easytickets.business.dto.OrderDto;

import java.util.Optional;

public interface OrderRepo {

    OrderDto save(OrderDto order);

    Optional<OrderDto> findById(String id);

    Optional<OrderDto> findByReservationId(String reservationId);
}
