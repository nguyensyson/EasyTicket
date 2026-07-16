package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class EventServiceUnavailableException extends BusinessException {

    public EventServiceUnavailableException(String message) {
        super(AppConstants.EVENT_SERVICE_UNAVAILABLE, message, HttpStatus.BAD_GATEWAY);
    }

    public EventServiceUnavailableException(String message, Throwable cause) {
        super(AppConstants.EVENT_SERVICE_UNAVAILABLE, message, HttpStatus.BAD_GATEWAY);
        initCause(cause);
    }
}
