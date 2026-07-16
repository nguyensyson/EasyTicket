package com.easytickets.business.services.impl;

import com.easytickets.business.dto.LocationDto;
import com.easytickets.business.repo.LocationRepo;
import com.easytickets.business.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepo locationRepo;

    @Override
    @Cacheable(cacheNames = "locations")
    public List<LocationDto> listLocations() {
        return locationRepo.findAll();
    }
}
