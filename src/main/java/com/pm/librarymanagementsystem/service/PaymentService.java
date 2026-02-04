package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentStatusResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Pageable;


public interface PaymentService {

    InitiatePaymentResponse initiatePayment(Long userId, InitiatePaymentRequest request);

    PaymentResponse getPaymentById(Long paymentId);

    PaymentStatusResponse getPaymentStatus(Long paymentId);

    PaymentResponse refundPayment(Long paymentId) throws BadRequestException;

    PageResponse<PaymentResponse> getPaymentHistory(Pageable pageable);

    Payment createSubscriptionRenewalPayment(Subscription subscription);

}
