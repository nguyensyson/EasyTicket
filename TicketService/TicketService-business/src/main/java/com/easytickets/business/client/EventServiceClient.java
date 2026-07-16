package com.easytickets.business.client;

import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "event-service", url = "${services.event-service.url}")
public interface EventServiceClient {

    @GetMapping("/api/v1/events/{eventId}/ticket-types")
    ApiResponse<List<TicketTypeDto>> getTicketTypes(@PathVariable("eventId") String eventId);
}
