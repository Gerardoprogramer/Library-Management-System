package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;

public interface PaymentGatewayService {
    GatewayPaymentResponse createCheckoutSession(
            Payment payment,
            InitiatePaymentRequest request
    );

    GatewayRefundResponse refundPayment(Payment payment);

    GatewayPaymentResponse createCheckoutSessionForRenewal(Payment payment);
}
