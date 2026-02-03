package com.pm.librarymanagementsystem.service.webhook;

import com.pm.librarymanagementsystem.configurations.StripeConfig;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.StripeWebhookEvent;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.StripeWebhookEventRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final PaymentRepository paymentRepository;
    private final StripeWebhookEventRepository webhookEventRepository;

    public void handleWebhook(String payload, String sigHeader) {

        Event event = constructEvent(payload, sigHeader);

        if (alreadyProcessed(event.getId())) {
            log.info("Webhook already processed: {}", event.getId());
            return;
        }

        processEvent(event);

        markEventProcessed(event);
    }

    private Event constructEvent(String payload, String sigHeader) {

        try {
            return Webhook.constructEvent(
                    payload,
                    sigHeader,
                    stripeConfig.getWebhookSecret()
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid Stripe webhook", e);
        }
    }

    private boolean alreadyProcessed(String eventId) {
        return webhookEventRepository.existsById(eventId);
    }

    private void markEventProcessed(Event event) {

        StripeWebhookEvent webhookEvent = new StripeWebhookEvent();
        webhookEvent.setEventId(event.getId());
        webhookEvent.setEventType(event.getType());
        webhookEvent.setProcessedAt(LocalDateTime.now());

        webhookEventRepository.save(webhookEvent);
    }

    private void processEvent(Event event) {

        switch (event.getType()) {

            case "checkout.session.completed" ->
                    handleCheckoutSessionCompleted(event);

            case "payment_intent.payment_failed" ->
                    handlePaymentFailed(event);

            default ->
                    log.info("Unhandled Stripe event: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {

        Optional<StripeObject> stripeObject =
                event.getDataObjectDeserializer().getObject();

        if (stripeObject.isEmpty()) return;

        Session session = (Session) stripeObject.get();

        String paymentIdStr = session.getMetadata().get("paymentId");

        if (paymentIdStr == null) return;

        Long paymentId = Long.parseLong(paymentIdStr);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();

        // Protección estado
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already SUCCESS: {}", paymentId);
            return;
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setTransactionId(session.getId());
        payment.setPaymentIntentId(session.getPaymentIntent());

        paymentRepository.save(payment);
    }

    private void handlePaymentFailed(Event event) {

        Optional<StripeObject> stripeObject =
                event.getDataObjectDeserializer().getObject();

        if (stripeObject.isEmpty()) return;

        PaymentIntent paymentIntent = (PaymentIntent) stripeObject.get();

        String paymentIdStr = paymentIntent.getMetadata().get("paymentId");

        if (paymentIdStr == null) return;

        Long paymentId = Long.parseLong(paymentIdStr);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) return;

        payment.setPaymentStatus(PaymentStatus.FAILED);

        if (paymentIntent.getLastPaymentError() != null) {
            payment.setFailureReason(
                    paymentIntent.getLastPaymentError().getMessage()
            );
        }

        paymentRepository.save(payment);
    }

}

