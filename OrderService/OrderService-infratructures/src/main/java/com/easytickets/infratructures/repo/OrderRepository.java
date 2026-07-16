package com.easytickets.infratructures.repo;

import com.easytickets.infratructures.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByReservationId(String reservationId);
}
