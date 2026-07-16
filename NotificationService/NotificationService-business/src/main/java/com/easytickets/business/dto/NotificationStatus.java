package com.easytickets.business.dto;

/**
 * Outcome of publishing the notification message to the SQS queue — NOT the
 * outcome of the actual email delivery, which is owned by the downstream
 * SQS consumer that calls AWS SES.
 */
public enum NotificationStatus {
    QUEUED,
    QUEUE_FAILED
}
