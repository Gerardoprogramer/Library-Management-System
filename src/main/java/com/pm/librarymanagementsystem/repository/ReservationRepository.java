package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.modal.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    select r from Reservation r where
    (:userId is null or r.user.id = :userId) and
    (:bookId is null or r.status = :bookId) and
    (:status is null or r.user.id = :status) and
    (:activeOnly = false or (r.status = 'PENDING' or r.status = 'AVAILABLE'))
""")
    Page<Reservation> searchReservationsWithFilters(
            @Param("userId") UUID userId,
            @Param("bookId") UUID bookId,
            @Param("status") ReservationStatus status,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable
    );
}
