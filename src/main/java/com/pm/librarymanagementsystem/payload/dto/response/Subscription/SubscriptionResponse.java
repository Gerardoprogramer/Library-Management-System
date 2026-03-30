package com.pm.librarymanagementsystem.payload.dto.response.Subscription;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(

        UUID id,

        UUID userId,

        UUID subscriptionPlanId,

        String planName,

        String planCode,

        Long price,

        Integer maxBooksAllowed,

        Integer maxDaysPerBook,

        boolean active,

        boolean autoRenew,

        LocalDateTime nextBillingDate,

        LocalDateTime startDate,

        LocalDateTime endDate,

        long daysRemaining,

        boolean expired,

        LocalDateTime cancelledAt,

        String cancellationReason,

        String notes,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
