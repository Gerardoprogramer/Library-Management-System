package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.EmailService;
import com.pm.librarymanagementsystem.service.RenewalNotification;
import com.pm.librarymanagementsystem.service.SubscriptionAutoRenewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionAutoRenewServiceImpl
        implements SubscriptionAutoRenewService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionRenewalProcessor renewalProcessor;
    private final EmailService emailService;

    @Override
    public void processAutoRenewals() {

        List<UUID> subscriptionIds =
                subscriptionRepository
                        .findSubscriptionIdsDueForRenewal(
                                LocalDateTime.now()
                        );

        log.info(
                "AutoRenew - Found {} subscriptions to process",
                subscriptionIds.size()
        );

        for (UUID subscriptionId
                : subscriptionIds) {

            try {

                RenewalNotification notification =
                        renewalProcessor.process(
                                subscriptionId
                        );

                if (notification == null) {
                    continue;
                }

                emailService
                        .sendRenewalPaymentRequiredEmail(
                                notification.email(),
                                notification.userName(),
                                notification.planName(),
                                notification.checkoutUrl(),
                                notification.endDate()
                        );

            }  catch (Exception exception) {

                log.error(
                        "AutoRenew failed for subscription {}",
                        subscriptionId,
                        exception
                );

                try {
                    renewalProcessor.recordFailure(
                            subscriptionId
                    );
                } catch (Exception failureRecordingException) {

                    log.error(
                            "Could not record AutoRenew failure for subscription {}",
                            subscriptionId,
                            failureRecordingException
                    );
                }
            }
        }
    }
}