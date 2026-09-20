package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionAutoRenewServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionRenewalProcessor renewalProcessor;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionAutoRenewServiceImpl autoRenewService;

    @Test
    void processAutoRenewals_shouldContinueWhenFailureRecordingAlsoFails() {

        UUID firstSubscriptionId =
                UUID.randomUUID();

        UUID secondSubscriptionId =
                UUID.randomUUID();

        when(subscriptionRepository
                .findSubscriptionIdsDueForRenewal(
                        any(LocalDateTime.class)
                ))
                .thenReturn(
                        List.of(
                                firstSubscriptionId,
                                secondSubscriptionId
                        )
                );

        when(renewalProcessor.process(
                firstSubscriptionId
        ))
                .thenThrow(
                        new RuntimeException(
                                "Stripe failure"
                        )
                );

        doThrow(
                new RuntimeException(
                        "Database failure"
                )
        )
                .when(renewalProcessor)
                .recordFailure(
                        firstSubscriptionId
                );

        when(renewalProcessor.process(
                secondSubscriptionId
        ))
                .thenReturn(null);

        autoRenewService.processAutoRenewals();

        verify(renewalProcessor)
                .process(firstSubscriptionId);

        verify(renewalProcessor)
                .recordFailure(firstSubscriptionId);

        verify(renewalProcessor)
                .process(secondSubscriptionId);
    }
}