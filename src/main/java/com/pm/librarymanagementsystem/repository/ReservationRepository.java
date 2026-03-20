package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
