package com.easytickets.application.controller;

import com.easytickets.business.dto.CreateEventRequest;
import com.easytickets.business.dto.EventCategory;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.business.dto.UpdateEventRequest;
import com.easytickets.business.services.EventService;
import com.easytickets.common.dto.ApiResponse;
import com.easytickets.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<EventDto>> createEvent(@Valid @RequestBody CreateEventRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        EventDto created = eventService.createEvent(request, jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<EventDto>> updateEvent(@PathVariable String eventId,
                                                              @Valid @RequestBody UpdateEventRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        EventDto updated = eventService.updateEvent(eventId, request, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable String eventId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        eventService.deleteEvent(eventId, jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventDto>> getEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getPublishedEvent(eventId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> searchEvents(
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        EventSearchCriteria criteria = EventSearchCriteria.builder()
                .category(category)
                .locationId(locationId)
                .from(from)
                .to(to)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(eventService.searchPublishedEvents(criteria)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<EventCategory>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.ok(eventService.listCategories()));
    }
}
