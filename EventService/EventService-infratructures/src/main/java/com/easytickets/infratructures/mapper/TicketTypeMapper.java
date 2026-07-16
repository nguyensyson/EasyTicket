package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.TicketTypeDto;
import com.easytickets.infratructures.model.TicketType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {
    TicketTypeDto toDto(TicketType entity);

    TicketType toEntity(TicketTypeDto dto);
}
