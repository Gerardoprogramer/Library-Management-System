package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentStatusResponse;

public class PaymentMapper {

    private PaymentMapper() {}

    /* =======================
       ENTITY → DTO
       ======================= */
    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) return null;

        return new PaymentResponse(
                payment.getId(),

                payment.getPaymentType(),
                payment.getPaymentStatus(),
                payment.getPaymentGateway(),

                payment.getAmount(),
                payment.getCurrency(),

                payment.getTransactionId(),
                payment.getCheckoutSessionId(),
                payment.getPaymentIntentId(),
                payment.getChargeId(),
                payment.getRefundId(),

                payment.getDescription(),
                payment.getFailureReason(),

                payment.getInitiatedAt(),
                payment.getCompletedAt(),
                payment.getRefundedAt(),

                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public static PaymentStatusResponse toStatusResponse(Payment payment) {
        if (payment == null) return null;

        return new PaymentStatusResponse(
                payment.getId(),
                payment.getPaymentStatus(),
                payment.getPaymentStatus() == com.pm.librarymanagementsystem.domain.PaymentStatus.SUCCESS
        );
    }
}
