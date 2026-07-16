package com.easytickets.business.services;

import com.easytickets.business.dto.CreateEventRequest;
import com.easytickets.business.dto.EventCategory;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.business.dto.UpdateEventRequest;
import com.easytickets.common.dto.PageResponse;

import java.util.List;

public interface EventService {

    EventDto createEvent(CreateEventRequest request, String organizerId);

    EventDto updateEvent(String eventId, UpdateEventRequest request, String organizerId);

    void deleteEvent(String eventId, String organizerId);

    EventDto getPublishedEvent(String eventId);

    PageResponse<EventDto> searchPublishedEvents(EventSearchCriteria criteria);

    List<EventCategory> listCategories();
}
