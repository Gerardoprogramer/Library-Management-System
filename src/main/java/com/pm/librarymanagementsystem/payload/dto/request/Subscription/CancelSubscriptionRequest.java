package com.pm.librarymanagementsystem.payload.dto.request.Subscription;

import jakarta.validation.constraints.NotBlank;

public record CancelSubscriptionRequest(

        @NotBlank(message = "El motivo de cancelación es obligatorio")
        String reason
) {
}
