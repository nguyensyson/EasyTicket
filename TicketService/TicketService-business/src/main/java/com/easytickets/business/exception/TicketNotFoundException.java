package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class TicketNotFoundException extends BusinessException {

    public TicketNotFoundException(String message) {
        super(AppConstants.TICKET_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
