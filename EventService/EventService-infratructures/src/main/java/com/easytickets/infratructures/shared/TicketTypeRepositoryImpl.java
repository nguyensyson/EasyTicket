package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.business.repo.TicketTypeRepo;
import com.easytickets.infratructures.mapper.TicketTypeMapper;
import com.easytickets.infratructures.repo.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
        // Cached qua @Cacheable(cacheNames = "ticket-types") - dùng ArrayList thay vì Stream.toList()
        // (immutable) vì GenericJacksonJsonRedisSerializer không ghi type-id cho List bất biến của
        // JDK khi serialize, khiến deserialize ở lần đọc cache tiếp theo ném MismatchedInputException.
        return new ArrayList<>(jpaRepository.findByEventId(eventId).stream().map(mapper::toDto).toList());
    }
}
