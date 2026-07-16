package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an organizer tries to modify configuration (ticket types, flash sale)
 * of an event that is no longer in DRAFT status.
 */
public class EventAlreadyPublishedException extends BusinessException {

    public EventAlreadyPublishedException(String message) {
        super(AppConstants.EVENT_ALREADY_PUBLISHED, message, HttpStatus.CONFLICT);
    }
}
