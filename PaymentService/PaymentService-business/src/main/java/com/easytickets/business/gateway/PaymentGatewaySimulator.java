package com.easytickets.business.gateway;

import java.util.function.Consumer;

/**
 * Stand-in for a real third-party payment gateway, which does not exist yet.
 * Instead of a real webhook round-trip, {@code simulate} asynchronously decides a
 * random outcome after a short delay and reports it back through {@code onResult}
 * (true = success, false = failed) – exercising the exact same code path
 * ({@code PaymentServiceImpl.processCallback}) a real gateway webhook would.
 * Implemented in {@code PaymentService-infratructures}.
 */
public interface PaymentGatewaySimulator {

    void simulate(String paymentId, Consumer<Boolean> onResult);
}
