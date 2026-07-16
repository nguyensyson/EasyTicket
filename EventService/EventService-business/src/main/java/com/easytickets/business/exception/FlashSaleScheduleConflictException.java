package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an event already has a flash sale scheduled (flash_sales.event_id is unique).
 */
public class FlashSaleScheduleConflictException extends BusinessException {

    public FlashSaleScheduleConflictException(String message) {
        super(AppConstants.FLASH_SALE_SCHEDULE_CONFLICT, message, HttpStatus.CONFLICT);
    }
}
