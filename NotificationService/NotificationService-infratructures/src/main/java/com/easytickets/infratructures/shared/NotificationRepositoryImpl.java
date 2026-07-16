package com.easytickets.infratructures.shared;

import com.easytickets.business.dto.NotificationDto;
import com.easytickets.business.dto.NotificationType;
import com.easytickets.business.repo.NotificationRepo;
import com.easytickets.infratructures.mapper.NotificationMapper;
import com.easytickets.infratructures.repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepo {

    private final NotificationRepository jpaRepository;
    private final NotificationMapper mapper;

    @Override
    public NotificationDto save(NotificationDto notification) {
        return mapper.toDto(jpaRepository.save(mapper.toEntity(notification)));
    }

    @Override
    public boolean existsByOrderIdAndType(String orderId, NotificationType type) {
        return jpaRepository.existsByOrderIdAndType(orderId, type);
    }
}
