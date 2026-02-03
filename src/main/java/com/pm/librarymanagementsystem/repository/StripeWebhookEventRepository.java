package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, String> {
}
