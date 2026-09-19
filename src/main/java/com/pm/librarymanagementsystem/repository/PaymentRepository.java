package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    Optional<Payment> findByIdAndUser_Id(
            UUID paymentId,
            UUID userId
    );

    Optional<Payment> findByCheckoutSessionIdAndUser_Id(
            String checkoutSessionId,
            UUID userId
    );
}