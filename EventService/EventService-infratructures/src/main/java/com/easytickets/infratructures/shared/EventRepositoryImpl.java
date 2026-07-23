package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.EventDto;
import com.easytickets.business.dto.EventSearchCriteria;
import com.easytickets.business.dto.EventStatus;
import com.easytickets.business.repo.EventRepo;
import com.easytickets.common.dto.PageResponse;
import com.easytickets.common.enums.RecordStatus;
import com.easytickets.infratructures.mapper.EventMapper;
import com.easytickets.infratructures.model.Event;
import com.easytickets.infratructures.repo.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepo {

    private final EventRepository jpaRepository;
    private final EventMapper mapper;

    @Override
    public EventDto save(EventDto event) {
        Event entity = mapper.toEntity(event);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public Optional<EventDto> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public PageResponse<EventDto> search(EventSearchCriteria criteria) {
        Specification<Event> spec = (root, query, cb) -> cb.equal(root.get("status"), EventStatus.PUBLISHED);

        if (criteria.getCategoryId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), criteria.getCategoryId()));
        }
        if (criteria.getLocationId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("locationId"), criteria.getLocationId()));
        }
        if (criteria.getFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), criteria.getFrom()));
        }
        if (criteria.getTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), criteria.getTo()));
        }

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), Sort.by("startTime").ascending());
        Page<Event> page = jpaRepository.findAll(spec, pageable);

        return PageResponse.<EventDto>builder()
                .content(page.getContent().stream().map(mapper::toDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    public void softDelete(String id) {
        jpaRepository.findById(id).ifPresent(event -> {
            event.setDeleteFlag(RecordStatus.DELETED);
            jpaRepository.save(event);
        });
    }

    @Override
    public List<EventDto> findByOrganizerId(String organizerId) {
        return jpaRepository.findByOrganizerId(organizerId).stream().map(mapper::toDto).toList();
    }
}
