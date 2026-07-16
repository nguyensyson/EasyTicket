package com.easytickets.application.controller;

import com.easytickets.business.dto.LocationDto;
import com.easytickets.business.services.LocationService;
import com.easytickets.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LocationDto>>> listLocations() {
        return ResponseEntity.ok(ApiResponse.ok(locationService.listLocations()));
    }
}
