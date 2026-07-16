package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String message) {
        super(AppConstants.USER_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
