package com.pm.librarymanagementsystem.modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stripe_webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StripeWebhookEvent {

    @Id
    private String eventId;

    private String eventType;

    private LocalDateTime processedAt;
}
