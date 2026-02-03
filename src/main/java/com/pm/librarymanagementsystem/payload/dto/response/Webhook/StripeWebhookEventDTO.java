package com.pm.librarymanagementsystem.payload.dto.response.Webhook;

public record StripeWebhookEventDTO (
        String id,
        String type
){
}
