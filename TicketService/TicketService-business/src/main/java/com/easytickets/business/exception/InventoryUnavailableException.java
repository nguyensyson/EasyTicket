package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class InventoryUnavailableException extends BusinessException {

    public InventoryUnavailableException(String message) {
        super(AppConstants.INVENTORY_UNAVAILABLE, message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
