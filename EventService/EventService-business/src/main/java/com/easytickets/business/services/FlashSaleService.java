package com.easytickets.business.services;

import com.easytickets.business.dto.CreateFlashSaleRequest;
import com.easytickets.business.dto.FlashSaleDto;

public interface FlashSaleService {

    FlashSaleDto createFlashSale(String eventId, CreateFlashSaleRequest request, String organizerId);
}
