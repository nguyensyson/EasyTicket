package com.easytickets.business.repo;

import com.easytickets.business.dto.TicketTypeDto;

import java.util.List;
import java.util.Optional;

public interface TicketTypeRepo {
    TicketTypeDto save(TicketTypeDto ticketType);

    Optional<TicketTypeDto> findById(String id);

    List<TicketTypeDto> findByEventId(String eventId);
}
