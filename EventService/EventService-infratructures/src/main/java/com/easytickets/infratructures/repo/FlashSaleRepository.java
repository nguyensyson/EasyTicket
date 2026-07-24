package com.easytickets.infratructures.repo;

import com.easytickets.infratructures.model.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleRepository extends JpaRepository<FlashSale, String> {
    Optional<FlashSale> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<FlashSale> findByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
            LocalDateTime startAt, LocalDateTime endAt);
}
