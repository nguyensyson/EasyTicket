package com.easytickets.business.client;

import com.easytickets.business.dto.EventOrderStatsDto;
import com.easytickets.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service", url = "${services.order-service.url}", configuration = FeignClientConfig.class)
public interface OrderServiceClient {

    @GetMapping("/api/v1/orders/stats/by-events")
    ApiResponse<List<EventOrderStatsDto>> getStatsByEvents(@RequestParam("eventIds") List<String> eventIds);
}
