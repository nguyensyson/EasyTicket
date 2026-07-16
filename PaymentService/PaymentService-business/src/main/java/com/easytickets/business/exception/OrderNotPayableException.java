package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class OrderNotPayableException extends BusinessException {

    public OrderNotPayableException(String message) {
        super(AppConstants.ORDER_NOT_PAYABLE, message, HttpStatus.CONFLICT);
    }
}
