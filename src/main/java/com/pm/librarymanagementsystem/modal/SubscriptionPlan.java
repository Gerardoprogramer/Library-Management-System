package com.pm.librarymanagementsystem.modal;

import com.pm.librarymanagementsystem.domain.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String planCode;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    @Positive(message = "El número máximo de libros debe ser positivo.")
    private Integer maxBooksAllowed;

    @Column(nullable = false)
    @Positive(message = "El número máximo de dias debe ser positivo.")
    private Integer maxDaysPerBook;

    private Integer displayOrder = 0;

    private Boolean active = true;

    private Boolean featured = false;

    private String badgeText;

    private String adminNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
