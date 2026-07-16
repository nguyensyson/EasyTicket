package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(String message) {
        super(AppConstants.ORDER_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
