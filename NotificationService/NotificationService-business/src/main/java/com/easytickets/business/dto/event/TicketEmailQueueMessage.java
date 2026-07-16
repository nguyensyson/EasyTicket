package com.easytickets.business.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message body published to the ticket-email SQS queue. A downstream consumer
 * (implemented separately) reads this queue and calls AWS SES to actually send
 * the ticket QR email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEmailQueueMessage {
    private String orderId;
    private String paymentId;
    private LocalDateTime paidAt;
    private String type;
}
