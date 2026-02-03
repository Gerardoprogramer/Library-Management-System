package com.pm.librarymanagementsystem.modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(
        name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_user", columnList = "user_id"),
                @Index(name = "idx_subscription_active", columnList = "active"),
                @Index(name = "idx_subscription_end_date", columnList = "end_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(nullable = false, length = 100)
    private String planName;

    @Column(nullable = false, length = 50)
    private String planCode;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer maxBooksAllowed;

    @Column(nullable = false)
    private Integer maxDaysPerBook;

    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean active = false;

    @Column(nullable = false)
    private boolean autoRenew = false;

    private LocalDateTime cancelledAt;

    @Column(length = 255)
    private String cancellationReason;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {
        initializeFromPlan();
    }

    public boolean isCurrentlyActive() {
        if (!active) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public long getDaysRemaining() {
        if (isExpired()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }

    public void cancel(String reason) {
        this.active = false;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    private void calculateEndDate() {
        if (subscriptionPlan != null && startDate != null) {
            this.endDate = startDate.plusDays(subscriptionPlan.getDurationDays());
        }
    }

    private void initializeFromPlan() {
        if (subscriptionPlan == null) {
            throw new IllegalStateException("SubscriptionPlan no puede ser null");
        }

        this.planName = subscriptionPlan.getName();
        this.planCode = subscriptionPlan.getPlanCode();
        this.price = subscriptionPlan.getPrice();
        this.maxBooksAllowed = subscriptionPlan.getMaxBooksAllowed();
        this.maxDaysPerBook = subscriptionPlan.getMaxDaysPerBook();

        if (this.startDate == null) {
            this.startDate = LocalDateTime.now();
        }

        calculateEndDate();
    }
}
