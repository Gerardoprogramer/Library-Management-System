package com.pm.librarymanagementsystem.modal;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.PaymentGateway;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_payment_user", columnList = "user_id"),
        @Index(name = "idx_payment_status", columnList = "paymentStatus"),
        @Index(name = "idx_payment_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentGateway paymentGateway;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(unique = true)
    private String transactionId;

    @Column(unique = true)
    private String gatewayPaymentId;

    @Column(unique = true)
    private String gatewayOrderId;

    @Column(nullable = true)
    private String gatewaySignature;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private String failureReason;

    private LocalDateTime initiatedAt;

    private LocalDateTime completedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
