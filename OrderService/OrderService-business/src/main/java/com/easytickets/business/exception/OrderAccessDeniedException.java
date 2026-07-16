package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class OrderAccessDeniedException extends BusinessException {

    public OrderAccessDeniedException(String message) {
        super(AppConstants.FORBIDDEN, message, HttpStatus.FORBIDDEN);
    }
}
