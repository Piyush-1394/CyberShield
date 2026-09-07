package com.cybershieldai.api.billing;

public interface PaymentGateway {
    TokenizedMethod tokenize(AddPaymentRequest request);

    record AddPaymentRequest(String brand, String last4, String methodType, String gatewayToken) {}

    record TokenizedMethod(String brand, String last4, String methodType, String gatewayToken) {}
}
