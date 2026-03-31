package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponseDTO;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentStatusResponse;
import com.stripe.exception.StripeException;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface PaymentService {

    InitiatePaymentResponse initiatePayment(UUID userId, InitiatePaymentRequest request);

    PaymentResponse getPaymentById(UUID paymentId);

    PaymentStatusResponse getPaymentStatus(UUID paymentId);

    PaymentResponse refundPayment(UUID paymentId) throws BadRequestException;

    PageResponse<PaymentResponse> getPaymentHistory(Pageable pageable);

    Payment createSubscriptionRenewalPayment(Subscription subscription);

    PaymentResponseDTO getPaymentDetails(String sessionId) throws StripeException;

}
