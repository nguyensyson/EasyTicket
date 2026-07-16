package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException(String message) {
        super(AppConstants.INVALID_CREDENTIALS, message, HttpStatus.UNAUTHORIZED);
    }
}
