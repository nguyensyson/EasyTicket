package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.LocationDto;
import com.easytickets.infratructures.model.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toDto(Location entity);

    Location toEntity(LocationDto dto);
}
