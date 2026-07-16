package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException(String message) {
        super(AppConstants.PAYMENT_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
