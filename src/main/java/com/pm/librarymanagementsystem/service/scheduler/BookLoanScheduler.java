package com.pm.librarymanagementsystem.service.scheduler;

import com.pm.librarymanagementsystem.service.BookLoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookLoanScheduler {

    private final BookLoanService bookLoanService;

    @Scheduled(cron = "${app.scheduler.overdue-loans-cron}")

    public void updateOverdueLoans() {

        int updated =
                bookLoanService
                        .updateOverdueBookLoan();

        if (updated > 0) {
            log.info(
                    "Overdue loans updated: {}",
                    updated
            );
        }
    }
}