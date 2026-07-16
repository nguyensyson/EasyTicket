package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class PaymentAccessDeniedException extends BusinessException {

    public PaymentAccessDeniedException(String message) {
        super(AppConstants.FORBIDDEN, message, HttpStatus.FORBIDDEN);
    }
}
