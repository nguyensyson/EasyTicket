package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class PaymentAlreadyProcessedException extends BusinessException {

    public PaymentAlreadyProcessedException(String message) {
        super(AppConstants.PAYMENT_ALREADY_PROCESSED, message, HttpStatus.CONFLICT);
    }
}
