package com.easytickets.business.services.impl;

import com.easytickets.business.dto.NotificationChannel;
import com.easytickets.business.dto.NotificationDto;
import com.easytickets.business.dto.NotificationStatus;
import com.easytickets.business.dto.NotificationType;
import com.easytickets.business.dto.event.PaymentSuccessEvent;
import com.easytickets.business.dto.event.TicketEmailQueueMessage;
import com.easytickets.business.exception.NotificationQueueFailedException;
import com.easytickets.business.producer.NotificationQueuePublisher;
import com.easytickets.business.repo.NotificationRepo;
import com.easytickets.business.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final String SYSTEM_ACTOR = "system";

    private final NotificationRepo notificationRepo;
    private final NotificationQueuePublisher notificationQueuePublisher;

    @Override
    public void processPaymentSuccess(PaymentSuccessEvent event) {
        if (notificationRepo.existsByOrderIdAndType(event.getOrderId(), NotificationType.TICKET_QR)) {
            log.info("Ticket email already queued, skipping duplicate. orderId={}", event.getOrderId());
            return;
        }

        TicketEmailQueueMessage message = TicketEmailQueueMessage.builder()
                .orderId(event.getOrderId())
                .paymentId(event.getPaymentId())
                .paidAt(event.getPaidAt())
                .type(NotificationType.TICKET_QR.name())
                .build();

        NotificationDto.NotificationDtoBuilder notification = NotificationDto.builder()
                .orderId(event.getOrderId())
                .type(NotificationType.TICKET_QR)
                .channel(NotificationChannel.EMAIL)
                .createdBy(SYSTEM_ACTOR);

        try {
            notificationQueuePublisher.publish(message);
            notificationRepo.save(notification
                    .status(NotificationStatus.QUEUED)
                    .queuedAt(LocalDateTime.now())
                    .build());
            log.info("Ticket email message queued. orderId={}, paymentId={}", event.getOrderId(), event.getPaymentId());
        } catch (Exception ex) {
            log.error("Failed to queue ticket email message. orderId={}", event.getOrderId(), ex);
            notificationRepo.save(notification
                    .status(NotificationStatus.QUEUE_FAILED)
                    .errorMessage(ex.getMessage())
                    .build());
            throw new NotificationQueueFailedException("Failed to queue ticket email notification for orderId=" + event.getOrderId());
        }
    }
}
