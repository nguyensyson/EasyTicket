package com.easytickets.business.services;

import com.easytickets.business.dto.event.PaymentSuccessEvent;

public interface NotificationService {
    void processPaymentSuccess(PaymentSuccessEvent event);
}
