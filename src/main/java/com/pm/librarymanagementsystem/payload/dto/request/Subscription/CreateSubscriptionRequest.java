package com.pm.librarymanagementsystem.payload.dto.request.Subscription;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSubscriptionRequest(

        @NotNull(message = "El plan de subscripción es obligatorio")
        UUID subscriptionPlanId,

        Boolean autoRenew,

        String notes
) {
}
