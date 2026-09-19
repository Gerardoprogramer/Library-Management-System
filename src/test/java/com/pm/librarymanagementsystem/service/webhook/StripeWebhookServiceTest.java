package com.pm.librarymanagementsystem.service.webhook;

import com.pm.librarymanagementsystem.configurations.StripeConfig;
import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.modal.*;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.StripeWebhookEventRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.EmailService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private StripeConfig stripeConfig;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeWebhookEventRepository webhookEventRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private StripeWebhookService stripeWebhookService;

    @BeforeEach
    void setUp() {
        when(stripeConfig.getWebhookSecret())
                .thenReturn("whsec_test");
    }

    @Test
    void handleWebhook_shouldRejectInvalidSignature() {

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "invalid-signature",
                            "whsec_test"
                    )
            ).thenThrow(
                    new RuntimeException(
                            "Invalid Stripe signature"
                    )
            );

            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> stripeWebhookService.handleWebhook(
                                    "{}",
                                    "invalid-signature"
                            )
                    );

            assertEquals(
                    "Firma de webhook de Stripe inválida",
                    exception.getMessage()
            );

            verifyNoInteractions(paymentRepository);
            verifyNoInteractions(subscriptionRepository);
            verifyNoInteractions(fineRepository);
            verifyNoInteractions(emailService);
        }
    }

    @Test
    void handleWebhook_shouldIgnoreAlreadyProcessedEvent() {

        Event event = mock(Event.class);

        when(event.getId())
                .thenReturn("evt_duplicate");

        when(webhookEventRepository.existsById(
                "evt_duplicate"
        )).thenReturn(true);

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "valid-signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            stripeWebhookService.handleWebhook(
                    "{}",
                    "valid-signature"
            );
        }

        verify(webhookEventRepository)
                .existsById("evt_duplicate");

        verify(paymentRepository, never())
                .findByIdForUpdate(any());

        verify(webhookEventRepository, never())
                .save(any());

        verifyNoInteractions(subscriptionRepository);
        verifyNoInteractions(fineRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void handleWebhook_shouldActivateSubscriptionWhenCheckoutIsPaid() {

        UUID paymentId = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("gera@gmail.com");
        user.setFullName("Gerardo Martínez");

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanName("Premium");
        subscription.setActive(false);
        subscription.setEndDate(
                LocalDateTime.now().plusDays(30)
        );

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setUser(user);
        payment.setPayable(subscription);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        Session session = mock(Session.class);

        when(session.getMetadata())
                .thenReturn(
                        Map.of(
                                "paymentId",
                                paymentId.toString()
                        )
                );

        when(session.getPaymentStatus())
                .thenReturn("paid");

        when(session.getId())
                .thenReturn("cs_test_123");

        when(session.getPaymentIntent())
                .thenReturn("pi_test_123");

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(deserializer.getObject())
                .thenReturn(Optional.of(session));

        Event event = mock(Event.class);

        when(event.getId())
                .thenReturn("evt_paid_subscription");

        when(event.getType())
                .thenReturn("checkout.session.completed");

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(webhookEventRepository.existsById(
                "evt_paid_subscription"
        )).thenReturn(false, false);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "valid-signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            stripeWebhookService.handleWebhook(
                    "{}",
                    "valid-signature"
            );
        }

        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getPaymentStatus()
        );

        assertTrue(subscription.isActive());

        assertEquals(
                "cs_test_123",
                payment.getTransactionId()
        );

        assertEquals(
                "cs_test_123",
                payment.getCheckoutSessionId()
        );

        assertEquals(
                "pi_test_123",
                payment.getPaymentIntentId()
        );

        assertNotNull(payment.getCompletedAt());

        verify(subscriptionRepository)
                .save(subscription);

        verify(paymentRepository)
                .save(payment);

        verify(emailService)
                .sendSubscriptionEmail(
                        eq("gera@gmail.com"),
                        eq("Gerardo Martínez"),
                        eq("Premium"),
                        eq(subscription.getEndDate())
                );

        ArgumentCaptor<StripeWebhookEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        StripeWebhookEvent.class
                );

        verify(webhookEventRepository)
                .save(eventCaptor.capture());

        StripeWebhookEvent savedEvent =
                eventCaptor.getValue();

        assertEquals(
                "evt_paid_subscription",
                savedEvent.getEventId()
        );

        assertEquals(
                "checkout.session.completed",
                savedEvent.getEventType()
        );

        assertNotNull(savedEvent.getProcessedAt());
    }

    @Test
    void handleWebhook_shouldNotActivateSubscriptionWhenCheckoutIsNotPaid() {

        UUID paymentId = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setActive(false);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setUser(user);
        payment.setPayable(subscription);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        Session session = mock(Session.class);

        when(session.getMetadata())
                .thenReturn(
                        Map.of(
                                "paymentId",
                                paymentId.toString()
                        )
                );

        when(session.getPaymentStatus())
                .thenReturn("unpaid");

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(deserializer.getObject())
                .thenReturn(Optional.of(session));

        Event event = mock(Event.class);

        when(event.getId())
                .thenReturn("evt_unpaid");

        when(event.getType())
                .thenReturn("checkout.session.completed");

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(webhookEventRepository.existsById(
                "evt_unpaid"
        )).thenReturn(false, false);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "valid-signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            stripeWebhookService.handleWebhook(
                    "{}",
                    "valid-signature"
            );
        }

        assertEquals(
                PaymentStatus.PENDING,
                payment.getPaymentStatus()
        );

        assertFalse(subscription.isActive());

        assertNull(payment.getCompletedAt());

        verify(paymentRepository, never())
                .save(any());

        verify(subscriptionRepository, never())
                .save(any());

        verifyNoInteractions(emailService);

        verify(webhookEventRepository)
                .save(any(StripeWebhookEvent.class));
    }

    @Test
    void handleWebhook_shouldMarkPaymentAsFailed() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        PaymentIntent paymentIntent =
                mock(PaymentIntent.class);

        when(paymentIntent.getMetadata())
                .thenReturn(
                        Map.of(
                                "paymentId",
                                paymentId.toString()
                        )
                );

        when(paymentIntent.getId())
                .thenReturn("pi_failed_123");

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(deserializer.getObject())
                .thenReturn(
                        Optional.of(paymentIntent)
                );

        Event event = mock(Event.class);

        when(event.getId())
                .thenReturn("evt_payment_failed");

        when(event.getType())
                .thenReturn(
                        "payment_intent.payment_failed"
                );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(webhookEventRepository.existsById(
                "evt_payment_failed"
        )).thenReturn(false);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "valid-signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            stripeWebhookService.handleWebhook(
                    "{}",
                    "valid-signature"
            );
        }

        assertEquals(
                PaymentStatus.FAILED,
                payment.getPaymentStatus()
        );

        assertEquals(
                "pi_failed_123",
                payment.getPaymentIntentId()
        );

        verify(paymentRepository)
                .save(payment);

        verify(webhookEventRepository)
                .save(any(StripeWebhookEvent.class));
    }

    @Test
    void handleWebhook_shouldNotOverrideSuccessfulPaymentWithLateFailedEvent() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentIntentId("pi_success");

        PaymentIntent paymentIntent =
                mock(PaymentIntent.class);

        when(paymentIntent.getMetadata())
                .thenReturn(
                        Map.of(
                                "paymentId",
                                paymentId.toString()
                        )
                );

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(deserializer.getObject())
                .thenReturn(
                        Optional.of(paymentIntent)
                );

        Event event = mock(Event.class);

        when(event.getId())
                .thenReturn("evt_late_failed");

        when(event.getType())
                .thenReturn(
                        "payment_intent.payment_failed"
                );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(webhookEventRepository.existsById(
                "evt_late_failed"
        )).thenReturn(false);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "valid-signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            stripeWebhookService.handleWebhook(
                    "{}",
                    "valid-signature"
            );
        }

        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getPaymentStatus()
        );

        assertEquals(
                "pi_success",
                payment.getPaymentIntentId()
        );

        verify(paymentRepository, never())
                .save(payment);

        verify(webhookEventRepository)
                .save(any(StripeWebhookEvent.class));
    }

    @Test
    void handleWebhook_shouldNotOverrideRefundedPaymentWithLateFailedEvent() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setPaymentIntentId("pi_refunded");

        PaymentIntent paymentIntent =
                mock(PaymentIntent.class);

        when(paymentIntent.getMetadata())
                .thenReturn(
                        Map.of(
                                "paymentId",
                                paymentId.toString()
                        )
                );

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(deserializer.getObject())
                .thenReturn(
                        Optional.of(paymentIntent)
                );

        Event event = mock(Event.class);

        when(event.getId())
                .thenReturn("evt_failed_after_refund");

        when(event.getType())
                .thenReturn(
                        "payment_intent.payment_failed"
                );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(webhookEventRepository.existsById(
                "evt_failed_after_refund"
        )).thenReturn(false);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "valid-signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            stripeWebhookService.handleWebhook(
                    "{}",
                    "valid-signature"
            );
        }

        assertEquals(
                PaymentStatus.REFUNDED,
                payment.getPaymentStatus()
        );

        verify(paymentRepository, never())
                .save(payment);

        verify(webhookEventRepository)
                .save(any(StripeWebhookEvent.class));
    }

    @Test
    void handleWebhook_shouldMarkFineAsPaidWhenCheckoutIsPaid() {

        UUID paymentId = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());

        Fine fine = new Fine();
        fine.setUser(user);
        fine.setStatus(FineStatus.PENDING);
        fine.setAmount(new BigDecimal("15.00"));

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setUser(user);
        payment.setPayable(fine);
        payment.setAmount(new BigDecimal("15.00"));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        Session session = mock(Session.class);

        when(session.getMetadata())
                .thenReturn(
                        Map.of(
                                "paymentId",
                                paymentId.toString()
                        )
                );

        when(session.getPaymentStatus())
                .thenReturn("paid");

        when(session.getId())
                .thenReturn("cs_fine_123");

        when(session.getPaymentIntent())
                .thenReturn("pi_fine_123");

        EventDataObjectDeserializer deserializer =
                mock(EventDataObjectDeserializer.class);

        when(deserializer.getObject())
                .thenReturn(
                        Optional.of(session)
                );

        Event event = mock(Event.class);

        when(event.getId())
                .thenReturn("evt_fine_paid");

        when(event.getType())
                .thenReturn(
                        "checkout.session.completed"
                );

        when(event.getDataObjectDeserializer())
                .thenReturn(deserializer);

        when(webhookEventRepository.existsById(
                "evt_fine_paid"
        )).thenReturn(false);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> webhook =
                     mockStatic(Webhook.class)) {

            webhook.when(() ->
                    Webhook.constructEvent(
                            "{}",
                            "valid-signature",
                            "whsec_test"
                    )
            ).thenReturn(event);

            stripeWebhookService.handleWebhook(
                    "{}",
                    "valid-signature"
            );
        }

        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getPaymentStatus()
        );

        assertEquals(
                FineStatus.PAID,
                fine.getStatus()
        );

        assertNotNull(
                fine.getPaidAt()
        );

        assertEquals(
                "cs_fine_123",
                fine.getTransactionId()
        );

        assertEquals(
                "cs_fine_123",
                payment.getTransactionId()
        );

        assertEquals(
                "pi_fine_123",
                payment.getPaymentIntentId()
        );

        verify(fineRepository)
                .save(fine);

        verify(paymentRepository)
                .save(payment);

        verify(subscriptionRepository, never())
                .save(any());

        verifyNoInteractions(emailService);
    }
}