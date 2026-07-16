package com.easytickets.infratructures.repo;

import com.easytickets.infratructures.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, String> {
    List<TicketType> findByEventId(String eventId);
}
