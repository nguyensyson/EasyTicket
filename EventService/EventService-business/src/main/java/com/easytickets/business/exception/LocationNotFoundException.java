package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class LocationNotFoundException extends BusinessException {

    public LocationNotFoundException(String message) {
        super(AppConstants.LOCATION_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
