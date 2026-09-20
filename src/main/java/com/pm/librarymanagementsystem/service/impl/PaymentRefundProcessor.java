package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.modal.Fine;
import com.pm.librarymanagementsystem.modal.Payable;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRefundProcessor {

    private final PaymentRepository paymentRepository;
    private final FineRepository fineRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Payment prepareRefund(
            UUID paymentId
    ) {

        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Pago no encontrado"
                                )
                        );

        if (payment.getPaymentStatus()
                == PaymentStatus.REFUNDED) {

            throw new BusinessRuleException(
                    "El pago ya fue reembolsado"
            );
        }

        if (payment.getPaymentStatus()
                != PaymentStatus.SUCCESS) {

            throw new BusinessRuleException(
                    "Solo los pagos exitosos pueden ser reembolsados"
            );
        }

        if (payment.getPaymentIntentId() == null
                || payment
                .getPaymentIntentId()
                .isBlank()) {

            throw new BusinessRuleException(
                    "El pago no tiene una transacción válida para reembolso"
            );
        }

        Hibernate.initialize(
                payment.getPayable()
        );

        return payment;
    }

    @Transactional
    public Payment applyRefund(
            UUID paymentId,
            GatewayRefundResponse refundResponse
    ) {

        Payment payment =
                paymentRepository
                        .findByIdForUpdate(paymentId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Pago no encontrado"
                                )
                        );

        /*
         * Revalidamos después del lock porque el estado
         * pudo cambiar entre prepareRefund() y Stripe.
         */
        if (payment.getPaymentStatus()
                == PaymentStatus.REFUNDED) {

            return payment;
        }

        if (payment.getPaymentStatus()
                != PaymentStatus.SUCCESS) {

            throw new BusinessRuleException(
                    "El pago ya no está disponible para reembolso"
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        payment.setRefundId(
                refundResponse.refundId()
        );

        payment.setRefundedAt(
                LocalDateTime.now()
        );

        rollbackPayableAfterRefund(
                payment
        );

        return payment;
    }

    private void rollbackPayableAfterRefund(
            Payment payment
    ) {

        Payable payable =
                (Payable) Hibernate.unproxy(
                        payment.getPayable()
                );

        if (payable instanceof Fine fine) {

            fine.reopenAfterRefund();

            fineRepository.save(fine);

        } else if (payable instanceof Subscription subscription) {

            subscription.cancel(
                    "Suscripción cancelada por reembolso"
            );

            subscription.setAutoRenew(false);

            subscriptionRepository.save(
                    subscription
            );
        }
    }
}