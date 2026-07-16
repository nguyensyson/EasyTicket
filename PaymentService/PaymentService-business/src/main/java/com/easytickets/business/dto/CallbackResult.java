package com.easytickets.business.dto;

/**
 * Outcome reported by the payment gateway webhook (or by the in-process
 * simulator standing in for a real gateway – see {@code PaymentGatewaySimulator}).
 */
public enum CallbackResult {
    SUCCESS,
    FAILED
}
