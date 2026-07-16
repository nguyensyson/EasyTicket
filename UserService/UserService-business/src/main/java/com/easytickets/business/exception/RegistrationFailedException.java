package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class RegistrationFailedException extends BusinessException {

    public RegistrationFailedException(String message) {
        super(AppConstants.REGISTRATION_FAILED, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
