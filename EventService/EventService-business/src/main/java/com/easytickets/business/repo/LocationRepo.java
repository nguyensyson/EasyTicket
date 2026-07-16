package com.easytickets.business.repo;

import com.easytickets.business.dto.LocationDto;

import java.util.List;
import java.util.Optional;

public interface LocationRepo {
    List<LocationDto> findAll();

    Optional<LocationDto> findById(String id);
}
