package com.pm.librarymanagementsystem.service;

import java.time.LocalDateTime;

public interface EmailService {

    void sendSubscriptionEmail(String to, String userName, String planName, LocalDateTime endDate);

    void sendPasswordResetEmail(String to, String userName, String resetLink);

    void sendRenewalPaymentRequiredEmail(
            String to,
            String userName,
            String planName,
            String renewalLink,
            LocalDateTime endDate
    );

}
