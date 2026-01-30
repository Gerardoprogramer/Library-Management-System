package com.pm.librarymanagementsystem.payload.dto.response.Subscription;

import java.time.LocalDateTime;

public record SubscriptionResponse(

        Long id,

        Long userId,

        Long subscriptionPlanId,

        String planName,

        String planCode,

        Long price,

        Integer maxBooksAllowed,

        Integer maxDaysPerBook,

        boolean active,

        boolean autoRenew,

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
