package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.EmailService;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.SubscriptionAutoRenewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionAutoRenewServiceImpl implements SubscriptionAutoRenewService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void processAutoRenewals() {

        List<Subscription> dueSubscriptions =
                subscriptionRepository.findSubscriptionsDueForRenewal(LocalDateTime.now());

        log.info("AutoRenew - Found {} subscriptions to process", dueSubscriptions.size());

        for (Subscription subscription : dueSubscriptions) {

            try {
                processSingleSubscriptionRenewal(subscription);

            } catch (Exception ex) {

                log.error("AutoRenew failed for subscription {}", subscription.getId(), ex);

                markRenewalAttemptFailed(subscription, ex.getMessage());
            }
        }
    }

    private void processSingleSubscriptionRenewal(Subscription subscription) {

        if (subscription.getRenewalAttemptCount() >= 3) {

            log.warn("Max renewal attempts reached for subscription {}", subscription.getId());

            subscription.setAutoRenew(false);
            subscription.setActive(false);

            subscriptionRepository.save(subscription);
            return;
        }

        log.info("Processing auto renew for subscription {}", subscription.getId());

        Payment payment = paymentService.createSubscriptionRenewalPayment(subscription);

        GatewayPaymentResponse gateway =
                paymentGatewayService.createCheckoutSessionForRenewal(payment);

        payment.setCheckoutSessionId(gateway.checkoutSessionId());
        payment.setPaymentIntentId(gateway.paymentIntentId());

        paymentRepository.save(payment);

        emailService.sendRenewalPaymentRequiredEmail(
                subscription.getUser().getEmail(),
                subscription.getUser().getFullName(),
                subscription.getPlanName(),
                gateway.checkoutUrl(),
                subscription.getEndDate()
        );


        subscription.setLastRenewalAttempt(LocalDateTime.now());
        subscription.setRenewalAttemptCount(
                subscription.getRenewalAttemptCount() + 1
        );

        subscriptionRepository.save(subscription);
    }

    private void markRenewalAttemptFailed(Subscription subscription, String reason) {

        subscription.setLastRenewalAttempt(LocalDateTime.now());
        subscription.setRenewalAttemptCount(
                subscription.getRenewalAttemptCount() + 1
        );

        subscriptionRepository.save(subscription);
    }
}
