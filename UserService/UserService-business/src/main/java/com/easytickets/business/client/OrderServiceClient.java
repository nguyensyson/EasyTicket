package com.easytickets.business.client;

import com.easytickets.business.dto.TicketHistoryDto;
import com.easytickets.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "order-service", url = "${services.order-service.url}", configuration = FeignClientConfig.class)
public interface OrderServiceClient {

    @GetMapping("/api/v1/orders/my-tickets")
    ApiResponse<List<TicketHistoryDto>> getMyTickets();
}
