package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class NotificationQueueFailedException extends BusinessException {

    public NotificationQueueFailedException(String message) {
        super(AppConstants.NOTIFICATION_QUEUE_FAILED, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
