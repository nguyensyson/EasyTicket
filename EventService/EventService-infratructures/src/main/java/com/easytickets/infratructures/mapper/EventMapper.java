package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.EventDto;
import com.easytickets.infratructures.model.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventDto toDto(Event entity);

    Event toEntity(EventDto dto);
}
