package com.pm.librarymanagementsystem.service.webhook;

import com.pm.librarymanagementsystem.configurations.StripeConfig;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.modal.*;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.StripeWebhookEventRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.EmailService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final SubscriptionRepository subscriptionRepository;
    private final StripeConfig stripeConfig;
    private final PaymentRepository paymentRepository;
    private final StripeWebhookEventRepository webhookEventRepository;
    private final EmailService emailService;
    private final FineRepository fineRepository;

    @Transactional
    public void handleWebhook(
            String payload,
            String sigHeader
    ) {
        Event event = constructEvent(
                payload,
                sigHeader
        );

        if (alreadyProcessed(event.getId())) {
            log.info(
                    "Stripe webhook already processed: {}",
                    event.getId()
            );
            return;
        }

        processEvent(event);

        // Segunda comprobación para entregas concurrentes.
        if (!alreadyProcessed(event.getId())) {
            markEventProcessed(event);
        }
    }

    private Event constructEvent(
            String payload,
            String sigHeader
    ) {
        try {
            return Webhook.constructEvent(
                    payload,
                    sigHeader,
                    stripeConfig.getWebhookSecret()
            );

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Firma de webhook de Stripe inválida",
                    exception
            );
        }
    }

    private void processEvent(Event event) {

        switch (event.getType()) {

            case "checkout.session.completed" ->
                    handleCheckoutSessionCompleted(event);

            case "payment_intent.payment_failed" ->
                    handlePaymentFailed(event);

            default ->
                    log.debug(
                            "Stripe event ignored: {}",
                            event.getType()
                    );
        }
    }

    private void handleCheckoutSessionCompleted(
            Event event
    ) {
        StripeObject stripeObject = getStripeObject(event);

        if (!(stripeObject instanceof Session session)) {
            throw new IllegalStateException(
                    "El evento no contiene una Checkout Session"
            );
        }

        UUID paymentId = extractPaymentId(
                session.getMetadata().get("paymentId"),
                event.getId()
        );

        if (paymentId == null) {
            return;
        }

        Payment payment = paymentRepository
                .findByIdForUpdate(paymentId)
                .orElse(null);

        if (payment == null) {
            log.warn(
                    "Payment {} not found for Stripe event {}",
                    paymentId,
                    event.getId()
            );
            return;
        }

        // Si estuvimos esperando el lock, otro webhook
        // puede haber procesado el evento mientras tanto.
        if (alreadyProcessed(event.getId())) {
            return;
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS
                || payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            log.info(
                    "Checkout Session {} completed without paid status",
                    session.getId()
            );
            return;
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setFailureReason(null);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setTransactionId(session.getId());
        payment.setCheckoutSessionId(session.getId());
        payment.setPaymentIntentId(session.getPaymentIntent());

        Payable payable =
                (Payable) Hibernate.unproxy(
                        payment.getPayable()
                );

        if (payable instanceof Subscription subscription) {

            if (payment.isRenewalPayment()) {
                handleSubscriptionRenewalSuccess(subscription);
            } else {
                activateSubscription(subscription);
            }

            emailService.sendSubscriptionEmail(
                    payment.getUser().getEmail(),
                    payment.getUser().getFullName(),
                    subscription.getPlanName(),
                    subscription.getEndDate()
            );

        } else if (payable instanceof Fine fine) {
            markFinePaid(
                    fine,
                    payment
            );
        }

        paymentRepository.save(payment);
    }

    private void handlePaymentFailed(
            Event event
    ) {
        StripeObject stripeObject = getStripeObject(event);

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            throw new IllegalStateException(
                    "El evento no contiene un PaymentIntent"
            );
        }

        UUID paymentId = extractPaymentId(
                paymentIntent.getMetadata().get("paymentId"),
                event.getId()
        );

        if (paymentId == null) {
            return;
        }

        Payment payment = paymentRepository
                .findByIdForUpdate(paymentId)
                .orElse(null);

        if (payment == null) {
            log.warn(
                    "Payment {} not found for Stripe event {}",
                    paymentId,
                    event.getId()
            );
            return;
        }

        if (alreadyProcessed(event.getId())) {
            return;
        }

        // Un evento atrasado nunca debe revertir
        // un pago exitoso o reembolsado.
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS
                || payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setPaymentIntentId(paymentIntent.getId());

        if (paymentIntent.getLastPaymentError() != null) {
            payment.setFailureReason(
                    paymentIntent
                            .getLastPaymentError()
                            .getMessage()
            );
        }

        paymentRepository.save(payment);
    }

    private StripeObject getStripeObject(Event event) {
        return event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se pudo deserializar el evento de Stripe"
                        )
                );
    }

    private UUID extractPaymentId(
            String paymentId,
            String eventId
    ) {
        if (paymentId == null || paymentId.isBlank()) {
            log.warn(
                    "Stripe event {} does not contain paymentId",
                    eventId
            );
            return null;
        }

        try {
            return UUID.fromString(paymentId);

        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Invalid paymentId '{}' in Stripe event {}",
                    paymentId,
                    eventId
            );
            return null;
        }
    }

    private boolean alreadyProcessed(String eventId) {
        return webhookEventRepository.existsById(eventId);
    }

    private void markEventProcessed(Event event) {

        StripeWebhookEvent webhookEvent =
                new StripeWebhookEvent();

        webhookEvent.setEventId(event.getId());
        webhookEvent.setEventType(event.getType());
        webhookEvent.setProcessedAt(LocalDateTime.now());

        webhookEventRepository.save(webhookEvent);
    }

    private void activateSubscription(
            Subscription subscription
    ) {
        if (subscription.isActive()) {
            return;
        }

        subscription.setActive(true);

        subscriptionRepository.save(subscription);
    }

    private void markFinePaid(
            Fine fine,
            Payment payment
    ) {
        if (fine.getStatus()
                != com.pm.librarymanagementsystem.domain.FineStatus.PENDING) {
            return;
        }

        fine.applyPayment(payment.getAmount());
        fine.setTransactionId(payment.getTransactionId());

        fineRepository.save(fine);
    }

    private void handleSubscriptionRenewalSuccess(
            Subscription subscription
    ) {
        subscription.setRenewalAttemptCount(0);
        subscription.setLastRenewalAttempt(null);

        subscription.setStartDate(
                subscription.getEndDate()
        );

        subscription.setEndDate(
                subscription.getEndDate()
                        .plusDays(
                                subscription
                                        .getSubscriptionPlan()
                                        .getDurationDays()
                        )
        );

        subscription.setNextBillingDate(
                subscription.getEndDate()
                        .minusDays(1)
        );

        subscription.setActive(true);

        subscriptionRepository.save(subscription);
    }
}
