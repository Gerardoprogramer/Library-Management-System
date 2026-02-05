package com.pm.librarymanagementsystem.modal;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.domain.BookLoanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_loan_user", columnList = "user_id"),
                @Index(name = "idx_loan_status", columnList = "status"),
                @Index(name = "idx_loan_return_date", columnList = "return_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookLoanType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookLoanStatus status;

    @Column(nullable = false)
    private LocalDateTime checkoutDate;

    @Column(nullable = false)
    private LocalDateTime returnDate;

    @Column(nullable = false)
    private Integer renewalCount = 0;

    @Column(nullable = false)
    private Integer maxRenewals = 2;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean overdue;

    @Column(nullable = false)
    private Integer overdueDays = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
