package com.pm.librarymanagementsystem.service.Scheduler;

import com.pm.librarymanagementsystem.service.ReservationQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ReservationQueueService reservationQueueService;

    @Scheduled(
            cron = "${app.scheduler.reservation-expiration-cron}"
    )
    public void expireReservations() {

        log.debug(
                "Checking expired reservations"
        );

        reservationQueueService
                .expireAvailableReservations();
    }
}