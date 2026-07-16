package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class PaymentAlreadyExistsException extends BusinessException {

    public PaymentAlreadyExistsException(String message) {
        super(AppConstants.PAYMENT_ALREADY_EXISTS, message, HttpStatus.CONFLICT);
    }
}
