package com.pm.librarymanagementsystem.service.scheduler;

import com.pm.librarymanagementsystem.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationScheduler {

    private final SubscriptionService subscriptionService;

    @Scheduled(
            cron = "${app.scheduler.subscription-expiration-cron}"
    )
    public void deactivateExpiredSubscriptions() {

        log.debug(
                "Checking expired subscriptions"
        );

        subscriptionService
                .deactivateExpiredSubscriptions();
    }
}