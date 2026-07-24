package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.FlashSaleDto;
import com.easytickets.business.repo.FlashSaleRepo;
import com.easytickets.infratructures.mapper.FlashSaleMapper;
import com.easytickets.infratructures.repo.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FlashSaleRepositoryImpl implements FlashSaleRepo {

    private final FlashSaleRepository jpaRepository;
    private final FlashSaleMapper mapper;

    @Override
    public FlashSaleDto save(FlashSaleDto flashSale) {
        return mapper.toDto(jpaRepository.save(mapper.toEntity(flashSale)));
    }

    @Override
    public Optional<FlashSaleDto> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId).map(mapper::toDto);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }

    @Override
    public List<FlashSaleDto> findActive(LocalDateTime now) {
        return jpaRepository.findByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(now, now).stream()
                .map(mapper::toDto)
                .toList();
    }
}
