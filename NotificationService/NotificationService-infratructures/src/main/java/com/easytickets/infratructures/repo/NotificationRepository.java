package com.easytickets.infratructures.repo;

import com.easytickets.business.dto.NotificationType;
import com.easytickets.infratructures.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    boolean existsByOrderIdAndType(String orderId, NotificationType type);
}
