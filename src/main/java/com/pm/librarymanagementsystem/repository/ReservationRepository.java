package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
    select case when count(r) > 0 then true else false end from Reservation r
    where r.user.id = :userId and r.book.id = :bookId
    and (r.status = 'PENDING' or r.status = 'AVAILABLE')
""")
    boolean hasActiveReservation(
            @Param("userId") UUID userId,
            @Param("bookId") UUID bookId
    );

    @Query("""
    select count(r) from Reservation r where r.user.id = :userId
    and (r.status = 'PENDING' or r.status = 'AVAILABLE')
""")
    long countActiveReservationsByUser(@Param("userId") UUID userId);



    @Query("""
     select count(r) from Reservation r where r.book.id = :bookId
     and r.status = 'PENDING'
""")
    long countPendingReservationByBook(@Param("bookId") UUID bookId);


    @Query("""
        select new com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse(
            r.id,
            b.id,
            b.title,
            b.author,
            b.coverImageUrl,
            u.id,
            r.status,
            r.queuePosition,
            r.notificationSent,
            r.notes,
            r.reservedAt,
            r.availableAt,
            r.availableUntil,
            r.cancelledAt,
            r.fulfilledAt,
            r.createdAt,
            r.updatedAt
        )
        from Reservation r
        join r.book b
        join r.user u
        where
        (:userId is null or u.id = :userId) and
        (:bookId is null or b.id = :bookId) and
        (:status is null or r.status = :status) and
        (:activeOnly = false or r.status in ('PENDING','AVAILABLE'))
""")
    Page<ReservationResponse> searchReservationsWithFilters(
            UUID userId,
            UUID bookId,
            ReservationStatus status,
            boolean activeOnly,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r
        from Reservation r
        where r.id = :reservationId
        """)
    Optional<Reservation> findByIdForUpdate(
            @Param("reservationId") UUID reservationId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservedAtAsc(
            UUID bookId,
            ReservationStatus status
    );

    long countByBookIdAndStatus(
            UUID bookId,
            ReservationStatus status
    );

    Optional<Reservation> findByIdAndUserId(
            UUID reservationId,
            UUID userId
    );

    List<Reservation> findByStatusAndAvailableUntilBefore(
            ReservationStatus status,
            LocalDateTime now
    );

    @Query("""
        select r
        from Reservation r
        where r.book.id = :bookId
        and r.status in :statuses
        order by r.reservedAt asc
        """)
    List<Reservation> findActiveQueue(
            @Param("bookId") UUID bookId,
            @Param("statuses") List<ReservationStatus> statuses
    );
}
