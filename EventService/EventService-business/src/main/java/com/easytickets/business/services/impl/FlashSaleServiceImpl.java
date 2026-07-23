package com.easytickets.business.services.impl;

import com.easytickets.business.dto.CreateFlashSaleRequest;
import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventStatus;
import com.easytickets.business.dto.FlashSaleDto;
import com.easytickets.business.dto.FlashSaleStatus;
import com.easytickets.business.exception.EventAccessDeniedException;
import com.easytickets.business.exception.EventAlreadyPublishedException;
import com.easytickets.business.exception.EventNotFoundException;
import com.easytickets.business.exception.FlashSaleScheduleConflictException;
import com.easytickets.business.exception.ValidationException;
import com.easytickets.business.repo.EventRepo;
import com.easytickets.business.repo.FlashSaleRepo;
import com.easytickets.business.services.FlashSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleRepo flashSaleRepo;
    private final EventRepo eventRepo;

    @Override
    public FlashSaleDto createFlashSale(String eventId, CreateFlashSaleRequest request, String organizerId) {
        EventDto event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new EventAccessDeniedException("You do not own this event: " + eventId);
        }
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventAlreadyPublishedException("Cannot schedule flash sale after the event is published: " + eventId);
        }
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new ValidationException("startAt must be before endAt");
        }
        if (flashSaleRepo.existsByEventId(eventId)) {
            throw new FlashSaleScheduleConflictException("Flash sale already scheduled for event: " + eventId);
        }

        FlashSaleDto flashSale = FlashSaleDto.builder()
                .eventId(eventId)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(FlashSaleStatus.SCHEDULED)
                .build();

        FlashSaleDto saved = flashSaleRepo.save(flashSale);
        log.info("Flash sale scheduled. eventId={}, flashSaleId={}, organizerId={}", eventId, saved.getId(), organizerId);
        return saved;
    }

    @Override
    public FlashSaleDto getFlashSale(String eventId) {
        return flashSaleRepo.findByEventId(eventId).orElse(null);
    }
}
