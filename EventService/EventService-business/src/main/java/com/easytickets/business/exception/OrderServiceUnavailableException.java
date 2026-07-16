package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class OrderServiceUnavailableException extends BusinessException {

    public OrderServiceUnavailableException(String message) {
        super(AppConstants.ORDER_SERVICE_UNAVAILABLE, message, HttpStatus.BAD_GATEWAY);
    }

    public OrderServiceUnavailableException(String message, Throwable cause) {
        super(AppConstants.ORDER_SERVICE_UNAVAILABLE, message, HttpStatus.BAD_GATEWAY);
        initCause(cause);
    }
}
