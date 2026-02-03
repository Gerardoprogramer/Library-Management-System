package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.service.webhook.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {

        stripeWebhookService.handleWebhook(payload, sigHeader);

        return ResponseEntity.ok("Webhook received");
    }
}
