package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private PaymentInitiationProcessor paymentInitiationProcessor;

    @Mock
    private PaymentRefundProcessor paymentRefundProcessor;

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
    void refundPayment_shouldNotChangeLocalStateWhenGatewayRefundFails() {

        UUID paymentId =
                UUID.randomUUID();

        Payment payment =
                new Payment();

        payment.setId(paymentId);
        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );
        payment.setPaymentIntentId(
                "pi_test_failed_refund"
        );

        when(paymentRefundProcessor
                .prepareRefund(paymentId))
                .thenReturn(payment);

        when(paymentGatewayService
                .refundPayment(payment))
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
                        () -> paymentService
                                .refundPayment(paymentId)
                );

        assertEquals(
                "No se pudo procesar el reembolso",
                exception.getMessage()
        );

        verify(paymentRefundProcessor)
                .prepareRefund(paymentId);

        verify(paymentGatewayService)
                .refundPayment(payment);

        verify(paymentRefundProcessor, never())
                .applyRefund(
                        any(),
                        any()
                );
    }

    @Test
    void initiatePayment_shouldCreateCheckoutOutsidePersistenceProcessor() {

        UUID paymentId =
                UUID.randomUUID();

        Payment payment =
                new Payment();

        payment.setId(paymentId);
        payment.setPaymentStatus(
                PaymentStatus.PENDING
        );

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        UUID.randomUUID(),
                        PaymentType.MEMBERSHIP
                );

        when(paymentInitiationProcessor
                .preparePayment(
                        userId,
                        request
                ))
                .thenReturn(payment);

        GatewayPaymentResponse gatewayResponse =
                new GatewayPaymentResponse(
                        "https://checkout.test/session",
                        "cs_test_123",
                        "pi_test_123"
                );

        when(paymentGatewayService
                .createCheckoutSession(payment))
                .thenReturn(gatewayResponse);

        InitiatePaymentResponse response =
                paymentService
                        .initiatePayment(
                                userId,
                                request
                        );

        assertEquals(
                paymentId,
                response.paymentId()
        );

        assertEquals(
                "cs_test_123",
                response.checkoutSessionId()
        );

        verify(paymentInitiationProcessor)
                .preparePayment(
                        userId,
                        request
                );

        verify(paymentGatewayService)
                .createCheckoutSession(payment);

        verify(paymentInitiationProcessor)
                .storeGatewayReferences(
                        paymentId,
                        gatewayResponse
                );
    }

    @Test
    void refundPayment_shouldDelegateToProcessorAndGateway() {

        UUID paymentId =
                UUID.randomUUID();

        Payment payment =
                new Payment();

        payment.setId(paymentId);
        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        GatewayRefundResponse gatewayResponse =
                new GatewayRefundResponse(
                        true,
                        "re_test_123",
                        "succeeded"
                );

        Payment refunded =
                new Payment();

        refunded.setId(paymentId);
        refunded.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        when(paymentRefundProcessor
                .prepareRefund(paymentId))
                .thenReturn(payment);

        when(paymentGatewayService
                .refundPayment(payment))
                .thenReturn(gatewayResponse);

        when(paymentRefundProcessor
                .applyRefund(
                        paymentId,
                        gatewayResponse
                ))
                .thenReturn(refunded);

        var response =
                paymentService
                        .refundPayment(
                                paymentId
                        );

        assertEquals(
                PaymentStatus.REFUNDED,
                response.paymentStatus()
        );

        verify(paymentRefundProcessor)
                .prepareRefund(paymentId);

        verify(paymentGatewayService)
                .refundPayment(payment);

        verify(paymentRefundProcessor)
                .applyRefund(
                        paymentId,
                        gatewayResponse
                );
    }
}