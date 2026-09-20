package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.RenewalNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionRenewalProcessor {

    private static final int MAX_RENEWAL_ATTEMPTS = 3;

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RenewalNotification process(
            UUID subscriptionId
    ) {

        Subscription subscription =
                subscriptionRepository
                        .findByIdForRenewal(
                                subscriptionId
                        )
                        .orElse(null);

        if (subscription == null) {
            return null;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (!subscription.isAutoRenew()
                || !subscription.isActive()
                || subscription.getNextBillingDate() == null
                || subscription
                .getNextBillingDate()
                .isAfter(now)) {

            return null;
        }

        int attempts =
                subscription.getRenewalAttemptCount()
                        != null
                        ? subscription
                        .getRenewalAttemptCount()
                        : 0;

        if (attempts >= MAX_RENEWAL_ATTEMPTS) {

            disableAutoRenewIfNeeded(
                    subscription,
                    now
            );

            return null;
        }

        boolean pendingRenewal =
                paymentRepository
                        .existsPendingRenewalPayment(
                                subscriptionId,
                                PaymentType.MEMBERSHIP,
                                PaymentStatus.PENDING
                        );

        if (pendingRenewal) {

            log.debug(
                    "Subscription {} already has a pending renewal payment",
                    subscriptionId
            );

            return null;
        }

        Payment payment =
                paymentService
                        .createSubscriptionRenewalPayment(
                                subscription
                        );

        GatewayPaymentResponse gateway =
                paymentGatewayService
                        .createCheckoutSession(
                                payment
                        );

        payment.setCheckoutSessionId(
                gateway.checkoutSessionId()
        );

        payment.setPaymentIntentId(
                gateway.paymentIntentId()
        );

        paymentRepository.save(payment);

        subscription.setLastRenewalAttempt(now);

        subscription.setRenewalAttemptCount(
                attempts + 1
        );

        subscriptionRepository.save(
                subscription
        );

        return new RenewalNotification(
                subscription.getUser().getEmail(),
                subscription.getUser().getFullName(),
                subscription.getPlanName(),
                gateway.checkoutUrl(),
                subscription.getEndDate()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
            UUID subscriptionId
    ) {

        Subscription subscription =
                subscriptionRepository
                        .findByIdForRenewal(
                                subscriptionId
                        )
                        .orElse(null);

        if (subscription == null) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        int attempts =
                subscription.getRenewalAttemptCount()
                        != null
                        ? subscription
                        .getRenewalAttemptCount()
                        : 0;

        int nextAttempt =
                attempts + 1;

        subscription.setRenewalAttemptCount(
                nextAttempt
        );

        subscription.setLastRenewalAttempt(
                now
        );

        if (nextAttempt >= MAX_RENEWAL_ATTEMPTS) {
            disableAutoRenewIfNeeded(
                    subscription,
                    now
            );
        }

        subscriptionRepository.save(
                subscription
        );
    }

    private void disableAutoRenewIfNeeded(
            Subscription subscription,
            LocalDateTime now
    ) {

        subscription.setAutoRenew(false);

        if (subscription.getEndDate() != null
                && !subscription
                .getEndDate()
                .isAfter(now)) {

            subscription.setActive(false);
        }

        subscriptionRepository.save(
                subscription
        );
    }
}