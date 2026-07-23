package com.easytickets.business.exception;

import com.easytickets.common.constant.AppConstants;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BusinessException {

    public CategoryNotFoundException(String message) {
        super(AppConstants.CATEGORY_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
