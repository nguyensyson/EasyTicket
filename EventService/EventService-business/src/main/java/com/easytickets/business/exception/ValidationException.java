package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

/**
 * Cross-field business validation that cannot be expressed with Bean Validation
 * annotations alone (e.g. endTime must be after startTime).
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(AppConstants.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST);
    }
}
