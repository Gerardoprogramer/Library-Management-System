package com.pm.librarymanagementsystem.payload.dto.request.Subscription;

import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(

        @NotNull(message = "El plan de suscripción es obligatorio")
        Long subscriptionPlanId,

        Boolean autoRenew,

        String notes
) {
}
