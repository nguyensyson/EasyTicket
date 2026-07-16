package com.easytickets.business.client;

import com.easytickets.business.dto.OrderSummaryDto;
import com.easytickets.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "${services.order-service.url}", configuration = FeignClientConfig.class)
public interface OrderServiceClient {

    @GetMapping("/api/v1/orders/{orderId}")
    ApiResponse<OrderSummaryDto> getOrder(@PathVariable("orderId") String orderId);
}
