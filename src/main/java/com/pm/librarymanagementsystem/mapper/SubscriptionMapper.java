package com.pm.librarymanagementsystem.mapper;

import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;

public class SubscriptionMapper {

    private SubscriptionMapper() {
    }

    /* =======================
       DTO → ENTITY
       ======================= */

    public static Subscription toEntity(
            CreateSubscriptionRequest request,
            User user,
            SubscriptionPlan plan
    ) {
        Subscription subscription = new Subscription();

        subscription.setUser(user);
        subscription.setSubscriptionPlan(plan);
        subscription.setAutoRenew(
                request.autoRenew() != null ? request.autoRenew() : false
        );
        subscription.setNotes(request.notes());

        return subscription;
    }

    /* =======================
       ENTITY → DTO (RESPONSE)
       ======================= */

    public static SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getSubscriptionPlan().getId(),
                subscription.getPlanName(),
                subscription.getPlanCode(),
                subscription.getPrice(),
                subscription.getMaxBooksAllowed(),
                subscription.getMaxDaysPerBook(),
                subscription.isActive(),
                subscription.isAutoRenew(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getDaysRemaining(),
                subscription.isExpired(),
                subscription.getCancelledAt(),
                subscription.getCancellationReason(),
                subscription.getNotes(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
