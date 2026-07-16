package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class ReservationFailedException extends BusinessException {

    public ReservationFailedException(String message) {
        super(AppConstants.RESERVATION_FAILED, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
