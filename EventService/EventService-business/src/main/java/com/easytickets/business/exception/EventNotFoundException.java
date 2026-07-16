package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class EventNotFoundException extends BusinessException {

    public EventNotFoundException(String message) {
        super(AppConstants.EVENT_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
