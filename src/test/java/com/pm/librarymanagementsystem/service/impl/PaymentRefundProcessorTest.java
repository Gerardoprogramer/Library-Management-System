package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.modal.Fine;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PaymentRefundProcessorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private PaymentRefundProcessor paymentRefundProcessor;

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
    void refundPayment_shouldRejectPendingPayment() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentRefundProcessor.prepareRefund(paymentId)
                );

        assertEquals(
                "Solo los pagos exitosos pueden ser reembolsados",
                exception.getMessage()
        );
    }

    @Test
    void refundPayment_shouldRejectAlreadyRefundedPayment() {

        UUID paymentId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatus.REFUNDED);

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentRefundProcessor.prepareRefund(paymentId)
                );

        assertEquals(
                "El pago ya fue reembolsado",
                exception.getMessage()
        );
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

        GatewayRefundResponse refundResponse =
                new GatewayRefundResponse(
                        true,
                        "re_test_fine",
                        "succeeded"
                );

        Payment response =
                paymentRefundProcessor.applyRefund(
                        paymentId,
                        refundResponse
                );

        assertEquals(
                PaymentStatus.REFUNDED,
                response.getPaymentStatus()
        );

        assertEquals(
                "re_test_fine",
                response.getRefundId()
        );

        assertNotNull(
                response.getRefundedAt()
        );

        assertEquals(
                FineStatus.PENDING,
                fine.getStatus()
        );

        assertNull(fine.getPaidAt());
        assertNull(fine.getTransactionId());

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

        GatewayRefundResponse refundResponse =
                new GatewayRefundResponse(
                        true,
                        "re_test_subscription",
                        "succeeded"
                );

        Payment response =
                paymentRefundProcessor.applyRefund(
                        paymentId,
                        refundResponse
                );

        assertEquals(
                PaymentStatus.REFUNDED,
                response.getPaymentStatus()
        );

        assertEquals(
                "re_test_subscription",
                response.getRefundId()
        );

        assertNotNull(
                response.getRefundedAt()
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

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentRefundProcessor.prepareRefund(paymentId)
                );

        assertEquals(
                "El pago no tiene una transacción válida para reembolso",
                exception.getMessage()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getPaymentStatus()
        );
    }
}
