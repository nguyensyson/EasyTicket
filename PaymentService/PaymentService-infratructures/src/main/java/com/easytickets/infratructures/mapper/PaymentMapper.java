package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.PaymentDto;
import com.easytickets.infratructures.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment entity);

    Payment toEntity(PaymentDto dto);
}
