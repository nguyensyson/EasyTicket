package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class TicketTypeNotFoundException extends BusinessException {

    public TicketTypeNotFoundException(String message) {
        super(AppConstants.TICKET_TYPE_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
