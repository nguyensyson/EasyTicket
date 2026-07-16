package com.easytickets.business.producer;

import com.easytickets.business.dto.event.TicketEmailQueueMessage;

/**
 * Port for publishing to the ticket-email SQS queue. Implemented in
 * {@code NotificationService-infratructures} with an SQS-client-backed adapter.
 * The downstream consumer of this queue (implemented separately) is
 * responsible for rendering the QR code and sending the email via AWS SES.
 */
public interface NotificationQueuePublisher {
    void publish(TicketEmailQueueMessage message);
}
