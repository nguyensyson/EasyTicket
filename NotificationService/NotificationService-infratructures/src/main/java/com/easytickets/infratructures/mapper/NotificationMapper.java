package com.easytickets.infratructures.mapper;

import com.easytickets.business.dto.NotificationDto;
import com.easytickets.infratructures.model.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationDto toDto(Notification entity);

    Notification toEntity(NotificationDto dto);
}
