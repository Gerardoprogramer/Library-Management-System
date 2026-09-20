package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.*;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.modal.*;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentInitiationProcessor {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FineRepository fineRepository;

    @Transactional
    public Payment preparePayment(
            UUID userId,
            InitiatePaymentRequest request
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuario no encontrado"
                        )
                );

        Payable payable =
                resolvePayable(
                        userId,
                        request
                );

        Payment payment =
                paymentRepository
                        .findFirstByUser_IdAndPayable_IdAndPaymentTypeAndPaymentStatusAndRenewalPaymentFalseOrderByCreatedAtDesc(
                                userId,
                                payable.getId(),
                                request.paymentType(),
                                PaymentStatus.PENDING
                        )
                        .orElseGet(() ->
                                paymentRepository.save(
                                        createPayment(
                                                user,
                                                payable,
                                                request.paymentType()
                                        )
                                )
                        );

        Hibernate.initialize(
                payment.getPayable()
        );

        return payment;
    }

    @Transactional
    public void storeGatewayReferences(
            UUID paymentId,
            GatewayPaymentResponse response
    ) {

        Payment payment =
                paymentRepository
                        .findByIdForUpdate(paymentId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Pago no encontrado"
                                )
                        );

        payment.setCheckoutSessionId(
                response.checkoutSessionId()
        );

        payment.setPaymentIntentId(
                response.paymentIntentId()
        );
    }

    private Payable resolvePayable(
            UUID userId,
            InitiatePaymentRequest request
    ) {

        return switch (request.paymentType()) {

            case MEMBERSHIP -> {

                Subscription subscription =
                        subscriptionRepository
                                .findByIdForPayment(
                                        request.payableId()
                                )
                                .orElseThrow(() ->
                                        new NotFoundException(
                                                "Suscripción no encontrada"
                                        )
                                );

                validateOwnership(
                        subscription,
                        userId
                );

                if (subscription.isCurrentlyActive()) {
                    throw new BusinessRuleException(
                            "La suscripción ya se encuentra activa"
                    );
                }

                yield subscription;
            }

            case FINE -> {

                Fine fine =
                        fineRepository
                                .findByIdForPayment(
                                        request.payableId()
                                )
                                .orElseThrow(() ->
                                        new NotFoundException(
                                                "Multa no encontrada"
                                        )
                                );

                validateOwnership(
                        fine,
                        userId
                );

                if (fine.getStatus()
                        != FineStatus.PENDING) {

                    throw new BusinessRuleException(
                            "La multa no está disponible para pago"
                    );
                }

                yield fine;
            }

            default ->
                    throw new BusinessRuleException(
                            "Tipo de pago no soportado"
                    );
        };
    }

    private void validateOwnership(
            Payable payable,
            UUID userId
    ) {

        if (payable.getUser() == null
                || !payable
                .getUser()
                .getId()
                .equals(userId)) {

            throw new NotFoundException(
                    "Recurso de pago no encontrado"
            );
        }
    }

    private Payment createPayment(
            User user,
            Payable payable,
            PaymentType paymentType
    ) {

        Payment payment =
                new Payment();

        payment.setUser(user);
        payment.setPayable(payable);
        payment.setPaymentType(paymentType);

        payment.setPaymentStatus(
                PaymentStatus.PENDING
        );

        payment.setPaymentGateway(
                PaymentGateway.STRIPE
        );

        payment.setInitiatedAt(
                LocalDateTime.now()
        );

        if (payable instanceof Subscription subscription) {

            payment.setAmount(
                    BigDecimal.valueOf(
                            subscription.getPrice(),
                            2
                    )
            );

            payment.setCurrency(
                    Currency.USD
            );

            payment.setDescription(
                    "Suscripción al plan: "
                            + subscription.getPlanName()
            );

        } else if (payable instanceof Fine fine) {

            payment.setAmount(
                    fine.getAmount()
            );

            payment.setCurrency(
                    fine.getCurrency()
            );

            payment.setDescription(
                    "Pago de multa"
            );
        }

        return payment;
    }
}