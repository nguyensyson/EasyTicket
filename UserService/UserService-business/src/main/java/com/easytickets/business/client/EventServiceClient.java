package com.easytickets.business.client;

import com.easytickets.business.dto.OrganizerHistoryDto;
import com.easytickets.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "event-service", url = "${services.event-service.url}", configuration = FeignClientConfig.class)
public interface EventServiceClient {

    @GetMapping("/api/v1/events/organizer-history")
    ApiResponse<OrganizerHistoryDto> getOrganizerHistory();
}
