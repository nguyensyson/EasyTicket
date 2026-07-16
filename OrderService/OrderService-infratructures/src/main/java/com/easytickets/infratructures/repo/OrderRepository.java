package com.easytickets.infratructures.repo;

import com.easytickets.business.dto.OrderStatus;
import com.easytickets.infratructures.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByReservationId(String reservationId);

    List<Order> findByUserId(String userId);

    @Query("SELECT o.eventId AS eventId, SUM(o.quantity) AS ticketsSold, SUM(o.totalAmount) AS revenue "
            + "FROM Order o WHERE o.status = :status AND o.eventId IN :eventIds GROUP BY o.eventId")
    List<EventOrderStatsProjection> sumPaidStatsByEventIds(@Param("status") OrderStatus status,
                                                            @Param("eventIds") List<String> eventIds);
}
