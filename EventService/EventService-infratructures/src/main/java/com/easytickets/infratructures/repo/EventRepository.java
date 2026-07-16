package com.easytickets.infratructures.repo;

import com.easytickets.infratructures.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, String>, JpaSpecificationExecutor<Event> {
    List<Event> findByOrganizerId(String organizerId);
}
