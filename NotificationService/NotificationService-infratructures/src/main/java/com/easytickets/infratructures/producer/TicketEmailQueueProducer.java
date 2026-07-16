package com.easytickets.infratructures.producer;

import com.easytickets.business.dto.event.TicketEmailQueueMessage;
import com.easytickets.business.producer.NotificationQueuePublisher;
import com.easytickets.infratructures.config.NotificationQueueProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

/**
 * Publishes ticket email messages to the SQS queue (app -> SQS -> SES). Sending the
 * actual email via AWS SES is handled by a separate downstream consumer of this queue.
 */
@Component
@RequiredArgsConstructor
public class TicketEmailQueueProducer implements NotificationQueuePublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final NotificationQueueProperties queueProperties;

    @Override
    public void publish(TicketEmailQueueMessage message) {
        try {
            String body = objectMapper.writeValueAsString(message);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueProperties.getTicketEmailUrl())
                    .messageBody(body)
                    .build());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send message to SQS queue, orderId=" + message.getOrderId(), ex);
        }
    }
}
