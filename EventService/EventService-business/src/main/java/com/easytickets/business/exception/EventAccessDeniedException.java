package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class EventAccessDeniedException extends BusinessException {

    public EventAccessDeniedException(String message) {
        super(AppConstants.EVENT_ACCESS_DENIED, message, HttpStatus.FORBIDDEN);
    }
}
