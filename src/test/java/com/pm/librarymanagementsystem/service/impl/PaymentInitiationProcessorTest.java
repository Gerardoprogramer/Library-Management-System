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
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentInitiationProcessorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private PaymentInitiationProcessor paymentInitiationProcessor;

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
    void preparePayment_shouldUseTrustedSubscriptionValues() {

        UUID subscriptionId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        Subscription subscription =
                new Subscription();

        subscription.setId(subscriptionId);
        subscription.setUser(user);
        subscription.setPrice(1299L);
        subscription.setPlanName("Premium");
        subscription.setActive(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findByIdForPayment(subscriptionId))
                .thenReturn(Optional.of(subscription));

        when(paymentRepository
                .findFirstByUser_IdAndPayable_IdAndPaymentTypeAndPaymentStatusAndRenewalPaymentFalseOrderByCreatedAtDesc(
                        userId,
                        subscriptionId,
                        PaymentType.MEMBERSHIP,
                        PaymentStatus.PENDING
                ))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {

                    Payment payment =
                            invocation.getArgument(0);

                    payment.setId(paymentId);

                    return payment;
                });

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        subscriptionId,
                        PaymentType.MEMBERSHIP
                );

        Payment payment =
                paymentInitiationProcessor
                        .preparePayment(
                                userId,
                                request
                        );

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

        when(subscriptionRepository.findByIdForPayment(subscriptionId))
                .thenReturn(Optional.of(subscription));

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        subscriptionId,
                        PaymentType.MEMBERSHIP
                );

        NotFoundException exception =
                assertThrows(
                        NotFoundException.class,
                        () -> paymentInitiationProcessor.preparePayment(
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

        when(subscriptionRepository.findByIdForPayment(subscriptionId))
                .thenReturn(Optional.of(subscription));

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        subscriptionId,
                        PaymentType.MEMBERSHIP
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentInitiationProcessor.preparePayment(
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

        when(fineRepository.findByIdForPayment(fineId))
                .thenReturn(Optional.of(fine));

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        fineId,
                        PaymentType.FINE
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> paymentInitiationProcessor.preparePayment(
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
    }

    @Test
    void preparePayment_shouldReuseExistingPendingPayment() {

        UUID subscriptionId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        Subscription subscription =
                new Subscription();

        subscription.setId(subscriptionId);
        subscription.setUser(user);
        subscription.setPrice(1299L);
        subscription.setPlanName("Premium");
        subscription.setActive(false);

        Payment existingPayment =
                new Payment();

        existingPayment.setId(paymentId);
        existingPayment.setUser(user);
        existingPayment.setPayable(subscription);

        existingPayment.setPaymentType(
                PaymentType.MEMBERSHIP
        );

        existingPayment.setPaymentStatus(
                PaymentStatus.PENDING
        );

        existingPayment.setAmount(
                new BigDecimal("12.99")
        );

        existingPayment.setCurrency(
                Currency.USD
        );

        existingPayment.setDescription(
                "Suscripción al plan: Premium"
        );

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(user)
                );

        when(subscriptionRepository.findByIdForPayment(subscriptionId))
                .thenReturn(Optional.of(subscription));

        when(paymentRepository
                .findFirstByUser_IdAndPayable_IdAndPaymentTypeAndPaymentStatusAndRenewalPaymentFalseOrderByCreatedAtDesc(
                        userId,
                        subscriptionId,
                        PaymentType.MEMBERSHIP,
                        PaymentStatus.PENDING
                ))
                .thenReturn(
                        Optional.of(existingPayment)
                );

        InitiatePaymentRequest request =
                new InitiatePaymentRequest(
                        subscriptionId,
                        PaymentType.MEMBERSHIP
                );

        Payment result =
                paymentInitiationProcessor
                        .preparePayment(
                                userId,
                                request
                        );

        assertSame(
                existingPayment,
                result
        );

        assertEquals(
                paymentId,
                result.getId()
        );

        assertEquals(
                PaymentStatus.PENDING,
                result.getPaymentStatus()
        );

        verify(
                paymentRepository,
                never()
        ).save(any(Payment.class));
    }
}