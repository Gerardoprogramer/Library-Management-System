package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.modal.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from Payment p
        where p.id = :paymentId
        """)
    Optional<Payment> findByIdForUpdate(
            @Param("paymentId") UUID paymentId
    );

    @Query("""
        select case when count(p) > 0 then true else false end
        from Payment p
        where p.payable.id = :subscriptionId
        and p.paymentType = :paymentType
        and p.paymentStatus = :paymentStatus
        and p.renewalPayment = true
        """)
    boolean existsPendingRenewalPayment(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("paymentType") PaymentType paymentType,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    Optional<Payment>
    findFirstByUser_IdAndPayable_IdAndPaymentTypeAndPaymentStatusAndRenewalPaymentFalseOrderByCreatedAtDesc(
            UUID userId,
            UUID payableId,
            PaymentType paymentType,
            PaymentStatus paymentStatus
    );
}