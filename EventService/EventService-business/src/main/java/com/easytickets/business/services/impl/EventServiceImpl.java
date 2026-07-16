package com.easytickets.business.services.impl;

import com.easytickets.business.dto.CreateEventRequest;
import com.easytickets.business.dto.EventCategory;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.business.dto.EventStatus;
import com.easytickets.business.dto.UpdateEventRequest;
import com.easytickets.business.exception.EventAccessDeniedException;
import com.easytickets.business.exception.EventNotFoundException;
import com.easytickets.business.exception.LocationNotFoundException;
import com.easytickets.business.exception.ValidationException;
import com.easytickets.business.repo.EventRepo;
import com.easytickets.business.repo.LocationRepo;
import com.easytickets.business.services.EventService;
import com.easytickets.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepo eventRepo;
    private final LocationRepo locationRepo;

    @Override
    public EventDto createEvent(CreateEventRequest request, String organizerId) {
        validateSchedule(request.getStartTime(), request.getEndTime());
        locationRepo.findById(request.getLocationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + request.getLocationId()));

        EventDto event = EventDto.builder()
                .organizerId(organizerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .locationId(request.getLocationId())
                .location(request.getLocation())
                .bannerUrl(request.getBannerUrl())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(EventStatus.DRAFT)
                .build();

        EventDto saved = eventRepo.save(event);
        log.info("Event created. eventId={}, organizerId={}", saved.getId(), organizerId);
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = "events", key = "#eventId")
    public EventDto updateEvent(String eventId, UpdateEventRequest request, String organizerId) {
        EventDto existing = getOwnedEvent(eventId, organizerId);
        validateSchedule(request.getStartTime(), request.getEndTime());
        locationRepo.findById(request.getLocationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + request.getLocationId()));

        EventDto updated = existing.toBuilder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .locationId(request.getLocationId())
                .location(request.getLocation())
                .bannerUrl(request.getBannerUrl())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(request.getStatus())
                .build();

        EventDto saved = eventRepo.save(updated);
        log.info("Event updated. eventId={}, organizerId={}, status={}", eventId, organizerId, saved.getStatus());
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = "events", key = "#eventId")
    public void deleteEvent(String eventId, String organizerId) {
        getOwnedEvent(eventId, organizerId);
        eventRepo.softDelete(eventId);
        log.info("Event deleted. eventId={}, organizerId={}", eventId, organizerId);
    }

    @Override
    @Cacheable(cacheNames = "events", key = "#eventId")
    public EventDto getPublishedEvent(String eventId) {
        EventDto event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventNotFoundException("Event not found: " + eventId);
        }
        return event;
    }

    @Override
    public PageResponse<EventDto> searchPublishedEvents(EventSearchCriteria criteria) {
        return eventRepo.search(criteria);
    }

    @Override
    public List<EventCategory> listCategories() {
        return Arrays.asList(EventCategory.values());
    }

    private EventDto getOwnedEvent(String eventId, String organizerId) {
        EventDto event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new EventAccessDeniedException("You do not own this event: " + eventId);
        }
        return event;
    }

    private void validateSchedule(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("startTime must be before endTime");
        }
    }
}
