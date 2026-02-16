package com.pm.librarymanagementsystem.modal;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_reservation_book", columnList = "book_id"),
                @Index(name = "idx_reservation_user", columnList = "user_id"),
                @Index(name = "idx_reservation_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    private Integer queuePosition;

    @Column(nullable = false)
    private Boolean notificationSent = false;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    private LocalDateTime availableAt;

    private LocalDateTime availableUntil;

    private LocalDateTime cancelledAt;

    private LocalDateTime fulfilledAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean canBeCancelled() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.AVAILABLE;
    }

    public boolean hasExpired(){
        return status == ReservationStatus.AVAILABLE
                && availableUntil != null
                && LocalDateTime.now().isAfter(availableUntil);
    }
}
