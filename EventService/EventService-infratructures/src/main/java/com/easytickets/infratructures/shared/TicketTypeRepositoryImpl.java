package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.repo.TicketTypeRepo;
import com.easytickets.infratructures.mapper.TicketTypeMapper;
import com.easytickets.infratructures.repo.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TicketTypeRepositoryImpl implements TicketTypeRepo {

    private final TicketTypeRepository jpaRepository;
    private final TicketTypeMapper mapper;

    @Override
    public TicketTypeDto save(TicketTypeDto ticketType) {
        return mapper.toDto(jpaRepository.save(mapper.toEntity(ticketType)));
    }

    @Override
    public Optional<TicketTypeDto> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }

    @Override
    public List<TicketTypeDto> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId).stream().map(mapper::toDto).toList();
    }
}
