package com.pm.librarymanagementsystem.payload.dto.request.Subscription;

import jakarta.validation.constraints.Positive;

public record UpdateSubscriptionRequest(
        Boolean active,

        Boolean autoRenew,

        @Positive(message = "El máximo de libros debe ser positivo")
        Integer maxBooksAllowed,

        @Positive(message = "El máximo de días por libro debe ser positivo")
        Integer maxDaysPerBook,

        String notes
) {
}
