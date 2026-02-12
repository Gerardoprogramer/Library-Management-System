package com.pm.librarymanagementsystem.modal;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_fine_status", columnList = "status"),
        @Index(name = "idx_fine_book_loan", columnList = "book_loan_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Fine extends Payable{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_loan_id", nullable = false)
    private BookLoan bookLoan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FineStatus status;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    private User waivedBy;

    @Column(name = "waiver_reason", length = 500)
    private String waiverReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "waived_at")
    private LocalDateTime waivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    public void applyPayment(BigDecimal paymentAmount){
        if(paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("El importe del pago debe ser positivo.");
        }

            this.status = FineStatus.PAID;
            this.paidAt = LocalDateTime.now();
    }

    public void waive(User admin, String reason){
        this.status = FineStatus.WAIVED;
        this.waivedBy = admin;
        this.waivedAt = LocalDateTime.now();
        this.waiverReason = reason;
    }
    }
