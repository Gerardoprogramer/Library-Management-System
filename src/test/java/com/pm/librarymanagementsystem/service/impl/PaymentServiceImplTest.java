package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.modal.Fine;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("gera@gmail.com");
    }

    @Test
    void initiatePayment_shouldUseTrustedSubscriptionValues() {

        UUID subscriptionId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setUser(user);
        subscription.setPrice(1299L);
        subscription.setPlanName("Premium");
        subscription.setActive(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findById(subscriptionId))
                .thenReturn(Optional.of(subscription));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setId(paymentId);
                    return payment;
                });

        when(paymentGatewayService.createCheckoutSession(any(Payment.class)))
                .thenReturn(
                        new GatewayPaymentResponse(
                                "https://checkout.test/session",
                                "cs_test_123",
                                "pi_test_123"
                        )
                );

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        subscriptionId,
                        PaymentType.MEMBERSHIP
                );

        InitiatePaymentResponse response =
                paymentService.initiatePayment(
                        userId,
                        request
                );

        ArgumentCaptor<Payment> paymentCaptor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository)
                .save(paymentCaptor.capture());

        Payment payment =
                paymentCaptor.getValue();

        assertEquals(
                new BigDecimal("12.99"),
                payment.getAmount()
        );

        assertEquals(
                Currency.USD,
                payment.getCurrency()
        );

        assertEquals(
                PaymentType.MEMBERSHIP,
                payment.getPaymentType()
        );

        assertEquals(
                PaymentStatus.PENDING,
                payment.getPaymentStatus()
        );

        assertEquals(
                subscription,
                payment.getPayable()
        );

        assertEquals(
                user,
                payment.getUser()
        );

        assertEquals(
                "Suscripción al plan: Premium",
                payment.getDescription()
        );

        assertEquals(
                paymentId,
                response.paymentId()
        );

        assertEquals(
                "https://checkout.test/session",
                response.checkoutUrl()
        );

        assertEquals(
                "cs_test_123",
                response.checkoutSessionId()
        );

        assertEquals(
                "cs_test_123",
                payment.getCheckoutSessionId()
        );

        assertEquals(
                "pi_test_123",
                payment.getPaymentIntentId()
        );
    }

    @Test
    void initiatePayment_shouldRejectPayableOwnedByAnotherUser() {

        UUID subscriptionId = UUID.randomUUID();

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setUser(otherUser);
        subscription.setActive(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findById(subscriptionId))
                .thenReturn(Optional.of(subscription));

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        subscriptionId,
                        PaymentType.MEMBERSHIP
                );

        NotFoundException exception =
                assertThrows(
                        NotFoundException.class,
                        () -> paymentService.initiatePayment(
                                userId,
                                request
                        )
                );

        assertEquals(
                "Recurso de pago no encontrado",
                exception.getMessage()
        );

        verify(paymentRepository, never())
                .save(any());

        verify(paymentGatewayService, never())
                .createCheckoutSession(any());
    }

    @Test
    void initiatePayment_shouldRejectActiveSubscription() {

        UUID subscriptionId = UUID.randomUUID();

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setUser(user);
        subscription.setActive(true);
        subscription.setStartDate(
                LocalDateTime.now().minusDays(1)
        );
        subscription.setEndDate(
                LocalDateTime.now().plusDays(20)
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findById(subscriptionId))
                .thenReturn(Optional.of(subscription));

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        subscriptionId,
                        PaymentType.MEMBERSHIP
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentService.initiatePayment(
                                userId,
                                request
                        )
                );

        assertEquals(
                "La suscripción ya se encuentra activa",
                exception.getMessage()
        );

        verify(paymentRepository, never())
                .save(any());

        verify(paymentGatewayService, never())
                .createCheckoutSession(any());
    }

    @Test
    void initiatePayment_shouldRejectFineThatIsNotPending() {

        UUID fineId = UUID.randomUUID();

        Fine fine = new Fine();
        fine.setId(fineId);
        fine.setUser(user);
        fine.setAmount(new BigDecimal("15.00"));
        fine.setCurrency(Currency.USD);
        fine.setStatus(FineStatus.PAID);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(fineRepository.findById(fineId))
                .thenReturn(Optional.of(fine));

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        fineId,
                        PaymentType.FINE
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentService.initiatePayment(
                                userId,
                                request
                        )
                );

        assertEquals(
                "La multa no está disponible para pago",
                exception.getMessage()
        );

        verify(paymentRepository, never())
                .save(any());

        verify(paymentGatewayService, never())
                .createCheckoutSession(any());
    }

    @Test
    void refundPayment_shouldRejectPendingPayment() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentService.refundPayment(paymentId)
                );

        assertEquals(
                "Solo los pagos exitosos pueden ser reembolsados",
                exception.getMessage()
        );

        verify(paymentGatewayService, never())
                .refundPayment(any());
    }

    @Test
    void refundPayment_shouldRejectAlreadyRefundedPayment() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.REFUNDED);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentService.refundPayment(paymentId)
                );

        assertEquals(
                "El pago ya fue reembolsado",
                exception.getMessage()
        );

        verify(paymentGatewayService, never())
                .refundPayment(any());
    }

    @Test
    void refundPayment_shouldRefundAndReopenFine() {

        UUID paymentId = UUID.randomUUID();

        Fine fine = new Fine();
        fine.setUser(user);
        fine.setStatus(FineStatus.PAID);
        fine.setAmount(new BigDecimal("15.00"));
        fine.setCurrency(Currency.USD);
        fine.setPaidAt(LocalDateTime.now());
        fine.setTransactionId("fine_transaction_123");

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setUser(user);
        payment.setPayable(fine);
        payment.setPaymentType(PaymentType.FINE);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setAmount(new BigDecimal("15.00"));
        payment.setCurrency(Currency.USD);
        payment.setPaymentIntentId("pi_test_fine");
        payment.setDescription("Pago de multa");

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentGatewayService.refundPayment(payment))
                .thenReturn(
                        new GatewayRefundResponse(
                                true,
                                "re_test_fine",
                                "Refund successful"
                        )
                );

        var response =
                paymentService.refundPayment(paymentId);

        assertEquals(
                PaymentStatus.REFUNDED,
                payment.getPaymentStatus()
        );

        assertEquals(
                "re_test_fine",
                payment.getRefundId()
        );

        assertNotNull(
                payment.getRefundedAt()
        );

        assertEquals(
                FineStatus.PENDING,
                fine.getStatus()
        );

        assertNull(
                fine.getPaidAt()
        );

        assertNull(
                fine.getTransactionId()
        );

        assertEquals(
                PaymentStatus.REFUNDED,
                response.paymentStatus()
        );

        verify(paymentGatewayService)
                .refundPayment(payment);

        verify(fineRepository)
                .save(fine);

        verify(subscriptionRepository, never())
                .save(any());
    }

    @Test
    void refundPayment_shouldRefundAndCancelSubscription() {

        UUID paymentId = UUID.randomUUID();

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanName("Premium");
        subscription.setPrice(1299L);
        subscription.setActive(true);
        subscription.setAutoRenew(true);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setUser(user);
        payment.setPayable(subscription);
        payment.setPaymentType(PaymentType.MEMBERSHIP);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setAmount(new BigDecimal("12.99"));
        payment.setCurrency(Currency.USD);
        payment.setPaymentIntentId("pi_test_subscription");
        payment.setDescription("Suscripción al plan: Premium");

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentGatewayService.refundPayment(payment))
                .thenReturn(
                        new GatewayRefundResponse(
                                true,
                                "re_test_subscription",
                                "Refund successful"
                        )
                );

        var response =
                paymentService.refundPayment(paymentId);

        assertEquals(
                PaymentStatus.REFUNDED,
                payment.getPaymentStatus()
        );

        assertEquals(
                "re_test_subscription",
                payment.getRefundId()
        );

        assertNotNull(
                payment.getRefundedAt()
        );

        assertFalse(
                subscription.isActive()
        );

        assertFalse(
                subscription.isAutoRenew()
        );

        assertEquals(
                "Suscripción cancelada por reembolso",
                subscription.getCancellationReason()
        );

        assertNotNull(
                subscription.getCancelledAt()
        );

        assertEquals(
                PaymentStatus.REFUNDED,
                response.paymentStatus()
        );

        verify(paymentGatewayService)
                .refundPayment(payment);

        verify(subscriptionRepository)
                .save(subscription);

        verify(fineRepository, never())
                .save(any());
    }

    @Test
    void refundPayment_shouldRejectSuccessfulPaymentWithoutPaymentIntentId() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentIntentId(null);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentService.refundPayment(paymentId)
                );

        assertEquals(
                "El pago no tiene una transacción válida para reembolso",
                exception.getMessage()
        );

        verify(paymentGatewayService, never())
                .refundPayment(any());

        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getPaymentStatus()
        );
    }

    @Test
    void refundPayment_shouldNotChangeLocalStateWhenGatewayRefundFails() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setUser(user);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentIntentId("pi_test_failed_refund");
        payment.setAmount(new BigDecimal("12.99"));
        payment.setCurrency(Currency.USD);

        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentGatewayService.refundPayment(payment))
                .thenReturn(
                        new GatewayRefundResponse(
                                false,
                                null,
                                "Stripe rejected refund"
                        )
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentService.refundPayment(paymentId)
                );

        assertEquals(
                "No se pudo procesar el reembolso",
                exception.getMessage()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getPaymentStatus()
        );

        assertNull(
                payment.getRefundId()
        );

        assertNull(
                payment.getRefundedAt()
        );

        verify(paymentGatewayService)
                .refundPayment(payment);

        verify(fineRepository, never())
                .save(any());

        verify(subscriptionRepository, never())
                .save(any());
    }
}