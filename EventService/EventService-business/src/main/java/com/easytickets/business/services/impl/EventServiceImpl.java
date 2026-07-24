package com.easytickets.business.services.impl;

import com.easytickets.business.client.OrderServiceClient;
import com.easytickets.business.dto.CategoryDto;
import com.easytickets.business.dto.CreateEventRequest;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventOrderStatsDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.business.dto.EventStatus;
import com.easytickets.business.dto.FlashSaleDto;
import com.easytickets.business.dto.OrganizerEventStatsDto;
import com.easytickets.business.dto.OrganizerHistoryDto;
import com.easytickets.business.dto.UpdateEventRequest;
import com.easytickets.business.exception.CategoryNotFoundException;
import com.easytickets.business.exception.EventAccessDeniedException;
import com.easytickets.business.exception.EventNotFoundException;
import com.easytickets.business.exception.LocationNotFoundException;
import com.easytickets.business.exception.OrderServiceUnavailableException;
import com.easytickets.business.exception.ValidationException;
import com.easytickets.business.repo.CategoryRepo;
import com.easytickets.business.repo.EventRepo;
import com.easytickets.business.repo.FlashSaleRepo;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private static final String FEATURED_CATEGORY_LABEL = "Nổi bật";
    private static final int FEATURED_LIMIT = 6;

    private final EventRepo eventRepo;
    private final LocationRepo locationRepo;
    private final CategoryRepo categoryRepo;
    private final FlashSaleRepo flashSaleRepo;
    private final OrderServiceClient orderServiceClient;

    @Override
    public EventDto createEvent(CreateEventRequest request, String organizerId) {
        validateSchedule(request.getStartTime(), request.getEndTime());
        locationRepo.findById(request.getLocationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + request.getLocationId()));
        CategoryDto category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));

        EventDto event = EventDto.builder()
                .organizerId(organizerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .categoryId(category.getId())
                .category(category.getName())
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
        CategoryDto category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));

        EventDto updated = existing.toBuilder()
                .title(request.getTitle())
                .description(request.getDescription())
                .categoryId(category.getId())
                .category(category.getName())
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
    public EventDto getManagedEvent(String eventId, String organizerId) {
        return getOwnedEvent(eventId, organizerId);
    }

    @Override
    public List<EventDto> listMyEvents(String organizerId) {
        return eventRepo.findByOrganizerId(organizerId);
    }

    @Override
    public PageResponse<EventDto> searchPublishedEvents(EventSearchCriteria criteria) {
        PageResponse<EventDto> result = eventRepo.search(criteria);

        boolean isFirstUnfilteredPage = criteria.getPage() == 0
                && criteria.getCategoryId() == null
                && criteria.getLocationId() == null
                && criteria.getFrom() == null
                && criteria.getTo() == null;
        if (!isFirstUnfilteredPage) {
            return result;
        }

        List<EventDto> featured = buildFeaturedEvents(result.getContent());
        if (featured.isEmpty()) {
            return result;
        }

        List<EventDto> content = new ArrayList<>(featured.size() + result.getContent().size());
        content.addAll(featured);
        content.addAll(result.getContent());

        return result.toBuilder().content(content).build();
    }

    /**
     * Featured = sự kiện đang trong khung giờ flash sale (proxy cho "lượt mua gần đây"
     * vì Event Service không lưu dữ liệu đơn hàng thật), lấp đầy chỗ trống còn lại bằng
     * các sự kiện sắp diễn ra sớm nhất của trang hiện tại. Category "Nổi bật" chỉ là
     * nhãn sinh ra ở tầng logic, không tồn tại trong bảng categories.
     */
    private List<EventDto> buildFeaturedEvents(List<EventDto> currentPageEvents) {
        List<String> activeFlashSaleEventIds = flashSaleRepo.findActive(LocalDateTime.now()).stream()
                .map(FlashSaleDto::getEventId)
                .distinct()
                .limit(FEATURED_LIMIT)
                .toList();

        List<EventDto> featuredSource = activeFlashSaleEventIds.stream()
                .map(eventRepo::findById)
                .flatMap(Optional::stream)
                .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                .collect(Collectors.toCollection(ArrayList::new));

        if (featuredSource.size() < FEATURED_LIMIT) {
            Set<String> alreadyFeatured = featuredSource.stream().map(EventDto::getId).collect(Collectors.toSet());
            currentPageEvents.stream()
                    .filter(event -> !alreadyFeatured.contains(event.getId()))
                    .limit(FEATURED_LIMIT - featuredSource.size())
                    .forEach(featuredSource::add);
        }

        return featuredSource.stream()
                .map(event -> event.toBuilder().category(FEATURED_CATEGORY_LABEL).build())
                .toList();
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

    private void validateSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("startTime must be before endTime");
        }
    }
}
