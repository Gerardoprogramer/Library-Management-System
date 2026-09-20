package com.pm.librarymanagementsystem.service;

import java.time.LocalDateTime;

public record RenewalNotification(
        String email,
        String userName,
        String planName,
        String checkoutUrl,
        LocalDateTime endDate
) {
}