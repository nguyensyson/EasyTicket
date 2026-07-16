package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class TicketSoldOutException extends BusinessException {

    public TicketSoldOutException(String message) {
        super(AppConstants.TICKET_SOLD_OUT, message, HttpStatus.CONFLICT);
    }
}
