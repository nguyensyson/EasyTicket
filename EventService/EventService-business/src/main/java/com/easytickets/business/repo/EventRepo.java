package com.easytickets.business.repo;

import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.common.dto.PageResponse;

import java.util.List;
import java.util.Optional;

public interface EventRepo {
    EventDto save(EventDto event);

    Optional<EventDto> findById(String id);

    PageResponse<EventDto> search(EventSearchCriteria criteria);

    void softDelete(String id);

    List<EventDto> findByOrganizerId(String organizerId);
}
