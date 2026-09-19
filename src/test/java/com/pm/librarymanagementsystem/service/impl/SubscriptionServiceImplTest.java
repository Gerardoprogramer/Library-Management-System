package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CancelSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionPostResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.repository.SubscriptionPlanRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cancelSubscription_shouldCancelOwnedSubscription() {

        UUID subscriptionId = UUID.randomUUID();

        User user = mock(User.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);
        Subscription subscription = mock(Subscription.class);

        when(user.getId()).thenReturn(userId);
        when(plan.getId()).thenReturn(UUID.randomUUID());

        when(subscription.getUser()).thenReturn(user);
        when(subscription.getSubscriptionPlan()).thenReturn(plan);

        when(subscriptionRepository.findByIdAndUser_Id(
                subscriptionId,
                userId
        )).thenReturn(Optional.of(subscription));

        CancelSubscriptionRequest request =
                new CancelSubscriptionRequest(
                        "Ya no deseo continuar"
                );

        subscriptionService.cancelSubscription(
                subscriptionId,
                request
        );

        verify(subscriptionRepository)
                .findByIdAndUser_Id(
                        subscriptionId,
                        userId
                );

        verify(subscription)
                .cancel("Ya no deseo continuar");

        verify(subscriptionRepository, never())
                .save(any());
    }

    @Test
    void cancelSubscription_shouldRejectSubscriptionFromAnotherUser() {

        UUID subscriptionId = UUID.randomUUID();

        when(subscriptionRepository.findByIdAndUser_Id(
                subscriptionId,
                userId
        )).thenReturn(Optional.empty());

        CancelSubscriptionRequest request =
                new CancelSubscriptionRequest(
                        "Intento cancelar"
                );

        assertThrows(
                NotFoundException.class,
                () -> subscriptionService.cancelSubscription(
                        subscriptionId,
                        request
                )
        );

        verify(subscriptionRepository)
                .findByIdAndUser_Id(
                        subscriptionId,
                        userId
                );

        verify(subscriptionRepository, never())
                .save(any());
    }


    @Test
    void subscribe_shouldCreateInactiveSubscriptionAndInitiateMembershipPayment() {

        UUID planId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        User user = mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(userService.getCurrentUserEntity()).thenReturn(user);

        SubscriptionPlan plan = new SubscriptionPlan();

        plan.setId(planId);
        plan.setName("Premium");
        plan.setPlanCode("PREMIUM");
        plan.setDurationDays(30);
        plan.setPrice(999L);
        plan.setCurrency(Currency.USD);
        plan.setMaxBooksAllowed(5);
        plan.setMaxDaysPerBook(14);

        when(subscriptionPlanRepository.findById(planId))
                .thenReturn(Optional.of(plan));

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> {

                    Subscription subscription =
                            invocation.getArgument(0);

                    subscription.setId(subscriptionId);

                    // Simulamos el @PrePersist que ejecutaría JPA
                    subscription.prePersist();

                    return subscription;
                });

        InitiatePaymentResponse paymentResponse =
                new InitiatePaymentResponse(
                        paymentId,
                        PaymentStatus.PENDING,
                        "https://checkout.test/session",
                        "cs_test_123"
                );

        when(paymentService.initiatePayment(
                eq(userId),
                any(InitiatePaymentRequest.class)
        )).thenReturn(paymentResponse);

        CreateSubscriptionRequest request =
                new CreateSubscriptionRequest(
                        planId,
                        true,
                        "Suscripción de prueba"
                );

        SubscriptionPostResponse response =
                subscriptionService.subscribe(request);

        assertEquals(subscriptionId, response.id());
        assertEquals("Premium", response.planName());
        assertFalse(response.active());
        assertEquals(
                "https://checkout.test/session",
                response.checkoutUrl()
        );

        ArgumentCaptor<InitiatePaymentRequest> paymentRequestCaptor =
                ArgumentCaptor.forClass(
                        InitiatePaymentRequest.class
                );

        verify(paymentService).initiatePayment(
                eq(userId),
                paymentRequestCaptor.capture()
        );

        InitiatePaymentRequest capturedRequest =
                paymentRequestCaptor.getValue();

        assertEquals(
                subscriptionId,
                capturedRequest.payableId()
        );

        assertEquals(
                PaymentType.MEMBERSHIP,
                capturedRequest.paymentType()
        );
    }
}