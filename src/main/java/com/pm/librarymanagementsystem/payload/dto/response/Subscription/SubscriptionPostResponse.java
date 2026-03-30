package com.pm.librarymanagementsystem.payload.dto.response.Subscription;

import java.util.UUID;

public record SubscriptionPostResponse(
        UUID id,
        String planName,
        boolean active,
        String checkoutUrl
) {
}
