package com.easytickets.business.services.impl;

import com.easytickets.business.client.OrderServiceClient;
import com.easytickets.business.dto.CreateEventRequest;
import com.easytickets.business.dto.EventCategory;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventOrderStatsDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.business.dto.EventStatus;
import com.easytickets.business.dto.OrganizerEventStatsDto;
import com.easytickets.business.dto.OrganizerHistoryDto;
import com.easytickets.business.dto.UpdateEventRequest;
import com.easytickets.business.exception.EventAccessDeniedException;
import com.easytickets.business.exception.EventNotFoundException;
import com.easytickets.business.exception.LocationNotFoundException;
import com.easytickets.business.exception.OrderServiceUnavailableException;
import com.easytickets.business.exception.ValidationException;
import com.easytickets.business.repo.EventRepo;
import com.easytickets.business.repo.LocationRepo;
import com.easytickets.business.services.EventService;
import com.easytickets.common.dto.PageResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepo eventRepo;
    private final LocationRepo locationRepo;
    private final OrderServiceClient orderServiceClient;

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

    @Override
    public OrganizerHistoryDto getOrganizerHistory(String organizerId) {
        List<EventDto> events = eventRepo.findByOrganizerId(organizerId);
        if (events.isEmpty()) {
            return OrganizerHistoryDto.builder()
                    .totalEvents(0)
                    .totalTicketsSold(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .events(List.of())
                    .build();
        }

        List<String> eventIds = events.stream().map(EventDto::getId).toList();
        Map<String, EventOrderStatsDto> statsByEventId;
        try {
            List<EventOrderStatsDto> stats = orderServiceClient.getStatsByEvents(eventIds).getData();
            statsByEventId = stats.stream().collect(Collectors.toMap(EventOrderStatsDto::getEventId, s -> s));
        } catch (FeignException ex) {
            log.error("Order Service unavailable while building organizer history. organizerId={}", organizerId, ex);
            throw new OrderServiceUnavailableException("Order Service is unavailable", ex);
        }

        List<OrganizerEventStatsDto> eventStats = events.stream()
                .map(event -> {
                    EventOrderStatsDto stat = statsByEventId.get(event.getId());
                    return OrganizerEventStatsDto.builder()
                            .eventId(event.getId())
                            .title(event.getTitle())
                            .status(event.getStatus())
                            .startTime(event.getStartTime())
                            .endTime(event.getEndTime())
                            .ticketsSold(stat != null ? stat.getTicketsSold() : 0)
                            .revenue(stat != null ? stat.getRevenue() : BigDecimal.ZERO)
                            .build();
                })
                .toList();

        long totalTicketsSold = eventStats.stream().mapToLong(OrganizerEventStatsDto::getTicketsSold).sum();
        BigDecimal totalRevenue = eventStats.stream()
                .map(OrganizerEventStatsDto::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrganizerHistoryDto.builder()
                .totalEvents(eventStats.size())
                .totalTicketsSold(totalTicketsSold)
                .totalRevenue(totalRevenue)
                .events(eventStats)
                .build();
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
