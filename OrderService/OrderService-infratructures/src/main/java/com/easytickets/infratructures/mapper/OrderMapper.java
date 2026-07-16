package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.OrderDto;
import com.easytickets.infratructures.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(Order entity);

    Order toEntity(OrderDto dto);
}
