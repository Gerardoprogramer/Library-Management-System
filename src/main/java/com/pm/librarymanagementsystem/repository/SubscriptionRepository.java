package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Subscription;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("select s from Subscription s where s.user.id = :userId AND " +
            "s.active = true and " +
            "s.startDate <= :today and s.endDate >= :today"
    )
    Optional<Subscription> findActiveSubscriptionByUserId(
            @Param("userId") UUID userId,
            @Param("today")LocalDateTime today
            );


    @Query("""
        select s.id
        from Subscription s
        where s.autoRenew = true
        and s.active = true
        and s.nextBillingDate <= :now
        order by s.nextBillingDate asc, s.id asc
        """)
    List<UUID> findSubscriptionIdsDueForRenewal(
            @Param("now") LocalDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s
        from Subscription s
        join fetch s.user
        join fetch s.subscriptionPlan
        where s.id = :subscriptionId
        """)
    Optional<Subscription> findByIdForRenewal(
            @Param("subscriptionId") UUID subscriptionId
    );

    Optional<Subscription> findByIdAndUser_Id(
            UUID id,
            UUID userId
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
        update Subscription s
        set s.active = false
        where s.active = true
        and s.endDate < :now
        """)
    int deactivateExpiredSubscriptions(
            @Param("now") LocalDateTime now
    );
}
