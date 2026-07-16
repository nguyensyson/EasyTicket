package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.UserProfileDto;
import com.easytickets.infratructures.model.UserProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfileDto toDto(UserProfile entity);

    UserProfile toEntity(UserProfileDto dto);
}
