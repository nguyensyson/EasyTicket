package com.easytickets.business.repo;

import com.easytickets.business.dto.FlashSaleDto;

import java.util.Optional;

public interface FlashSaleRepo {
    FlashSaleDto save(FlashSaleDto flashSale);

    Optional<FlashSaleDto> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
