package com.pm.librarymanagementsystem.service.Scheduler;

import com.pm.librarymanagementsystem.service.SubscriptionAutoRenewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoRenewScheduler {

    private final SubscriptionAutoRenewService autoRenewService;

    @Scheduled(cron = "0 */30 * * * *")
    public void runAutoRenew() {

        log.info("Running AutoRenew Scheduler");

        autoRenewService.processAutoRenewals();
    }
}

