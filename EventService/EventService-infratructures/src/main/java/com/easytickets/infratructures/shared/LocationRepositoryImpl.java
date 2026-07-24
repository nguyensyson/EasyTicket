package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.LocationDto;
import com.easytickets.business.repo.LocationRepo;
import com.easytickets.infratructures.mapper.LocationMapper;
import com.easytickets.infratructures.repo.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepo {

    private final LocationRepository jpaRepository;
    private final LocationMapper mapper;

    @Override
    public List<LocationDto> findAll() {
        // Cached qua @Cacheable(cacheNames = "locations") - dùng ArrayList thay vì Stream.toList()
        // (immutable) vì GenericJacksonJsonRedisSerializer không ghi type-id cho List bất biến của
        // JDK khi serialize, khiến deserialize ở lần đọc cache tiếp theo ném MismatchedInputException.
        return new ArrayList<>(jpaRepository.findAll().stream().map(mapper::toDto).toList());
    }

    @Override
    public Optional<LocationDto> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDto);
    }
}
