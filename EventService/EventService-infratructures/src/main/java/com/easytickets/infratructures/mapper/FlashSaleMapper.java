package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.FlashSaleDto;
import com.easytickets.infratructures.model.FlashSale;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FlashSaleMapper {
    FlashSaleDto toDto(FlashSale entity);

    FlashSale toEntity(FlashSaleDto dto);
}
