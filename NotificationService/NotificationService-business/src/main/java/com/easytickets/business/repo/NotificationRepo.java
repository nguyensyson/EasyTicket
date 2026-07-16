package com.easytickets.business.repo;

import com.easytickets.business.dto.NotificationDto;
import com.easytickets.business.dto.NotificationType;

public interface NotificationRepo {

    NotificationDto save(NotificationDto notification);

    boolean existsByOrderIdAndType(String orderId, NotificationType type);
}
