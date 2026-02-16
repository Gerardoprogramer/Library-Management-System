package com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan;

import com.pm.librarymanagementsystem.domain.Currency;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionPlanResponse(
        UUID id,

        String planCode,

        String name,

        String description,

        Integer durationDays,

        Long price,

        Currency currency,

        Integer maxBooksAllowed,

        Integer maxDaysPerBook,

        Integer displayOrder,

        Boolean active,

        Boolean featured,

        String badgeText,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
