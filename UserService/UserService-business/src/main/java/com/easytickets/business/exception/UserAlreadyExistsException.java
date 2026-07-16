package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(AppConstants.USER_ALREADY_EXISTS, message, HttpStatus.CONFLICT);
    }
}
