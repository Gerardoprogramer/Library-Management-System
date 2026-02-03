package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.configurations.StripeConfig;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeInitializer {

    private final StripeConfig stripeConfig;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeConfig.getSecretKey();
    }
}