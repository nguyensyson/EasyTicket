package com.easytickets.business.services;

import com.easytickets.business.dto.CreateEventRequest;
import com.easytickets.business.dto.EventCategory;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.business.dto.OrganizerHistoryDto;
import com.easytickets.business.dto.UpdateEventRequest;
import com.easytickets.common.dto.PageResponse;

import java.util.List;

public interface EventService {

    EventDto createEvent(CreateEventRequest request, String organizerId);

    EventDto updateEvent(String eventId, UpdateEventRequest request, String organizerId);

    void deleteEvent(String eventId, String organizerId);

    EventDto getPublishedEvent(String eventId);

    /**
     * Fetch a single event owned by the caller, regardless of status — used by the
     * Organizer management UI (draft/cancelled events are not visible via
     * {@link #getPublishedEvent}).
     */
    EventDto getManagedEvent(String eventId, String organizerId);

    /**
     * All events owned by the caller, regardless of status — used to render the
     * Organizer's "My events" list.
     */
    List<EventDto> listMyEvents(String organizerId);

    PageResponse<EventDto> searchPublishedEvents(EventSearchCriteria criteria);

    List<EventCategory> listCategories();

    /**
     * Aggregated stats (events, tickets sold, revenue) across every event owned by
     * this organizer. Ticket/revenue figures come from Order Service (batch, not
     * real-time) – used by User Service's {@code /me/organizer-history} endpoint.
     */
    OrganizerHistoryDto getOrganizerHistory(String organizerId);
}
