package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationQueueServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReservationQueueServiceImpl reservationQueueService;

    private UUID bookId;
    private Book book;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();

        book = new Book();
        book.setId(bookId);
        book.setTitle("Clean Code");
        book.setTotalCopies(5);
        book.setAvailableCopies(2);
        book.setActive(true);
    }

    @Test
    void promoteNextReservations_shouldPromotePendingReservationWhenCopyIsFree() {

        Reservation pending = new Reservation();
        pending.setId(UUID.randomUUID());
        pending.setBook(book);
        pending.setStatus(ReservationStatus.PENDING);
        pending.setReservedAt(
                LocalDateTime.now().minusHours(2)
        );
        pending.setNotificationSent(true);

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(0L);

        when(reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(
                        bookId,
                        ReservationStatus.PENDING
                ))
                .thenReturn(Optional.of(pending));

        when(reservationRepository.findActiveQueue(
                eq(bookId),
                eq(List.of(
                        ReservationStatus.AVAILABLE,
                        ReservationStatus.PENDING
                ))
        )).thenReturn(List.of(pending));

        reservationQueueService
                .promoteNextReservations(book);

        assertEquals(
                ReservationStatus.AVAILABLE,
                pending.getStatus()
        );

        assertNotNull(pending.getAvailableAt());
        assertNotNull(pending.getAvailableUntil());

        assertEquals(
                pending.getAvailableAt().plusHours(48),
                pending.getAvailableUntil()
        );

        assertFalse(
                pending.getNotificationSent()
        );

        assertEquals(
                1,
                pending.getQueuePosition()
        );
    }

    @Test
    void promoteNextReservations_shouldPromoteOnlyAsManyReservationsAsFreeCopies() {

        book.setAvailableCopies(2);

        Reservation first =
                pendingReservation(3);

        Reservation second =
                pendingReservation(2);

        Reservation third =
                pendingReservation(1);

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(0L);

        when(reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(
                        bookId,
                        ReservationStatus.PENDING
                ))
                .thenReturn(
                        Optional.of(first),
                        Optional.of(second)
                );

        when(reservationRepository.findActiveQueue(
                eq(bookId),
                eq(List.of(
                        ReservationStatus.AVAILABLE,
                        ReservationStatus.PENDING
                ))
        )).thenReturn(
                List.of(
                        first,
                        second,
                        third
                )
        );

        reservationQueueService
                .promoteNextReservations(book);

        assertEquals(
                ReservationStatus.AVAILABLE,
                first.getStatus()
        );

        assertEquals(
                ReservationStatus.AVAILABLE,
                second.getStatus()
        );

        assertEquals(
                ReservationStatus.PENDING,
                third.getStatus()
        );

        assertEquals(1, first.getQueuePosition());
        assertEquals(2, second.getQueuePosition());
        assertEquals(3, third.getQueuePosition());

        verify(
                reservationRepository,
                times(2)
        ).findFirstByBookIdAndStatusOrderByReservedAtAsc(
                bookId,
                ReservationStatus.PENDING
        );
    }

    @Test
    void promoteNextReservations_shouldNotPromoteWhenAllCopiesAreAlreadyReserved() {

        book.setAvailableCopies(2);

        Reservation available =
                new Reservation();

        available.setId(UUID.randomUUID());
        available.setBook(book);
        available.setStatus(
                ReservationStatus.AVAILABLE
        );
        available.setReservedAt(
                LocalDateTime.now().minusHours(2)
        );

        Reservation pending =
                pendingReservation(1);

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(2L);

        when(reservationRepository.findActiveQueue(
                eq(bookId),
                eq(List.of(
                        ReservationStatus.AVAILABLE,
                        ReservationStatus.PENDING
                ))
        )).thenReturn(
                List.of(
                        available,
                        pending
                )
        );

        reservationQueueService
                .promoteNextReservations(book);

        assertEquals(
                ReservationStatus.AVAILABLE,
                available.getStatus()
        );

        assertEquals(
                ReservationStatus.PENDING,
                pending.getStatus()
        );

        assertEquals(
                1,
                available.getQueuePosition()
        );

        assertEquals(
                2,
                pending.getQueuePosition()
        );

        verify(
                reservationRepository,
                never()
        ).findFirstByBookIdAndStatusOrderByReservedAtAsc(
                any(),
                any()
        );
    }

    @Test
    void expireAvailableReservations_shouldExpireAndPromoteNextReservation() {

        book.setAvailableCopies(1);

        UUID expiredReservationId =
                UUID.randomUUID();

        Reservation expired =
                new Reservation();

        expired.setId(expiredReservationId);
        expired.setBook(book);
        expired.setStatus(
                ReservationStatus.AVAILABLE
        );
        expired.setReservedAt(
                LocalDateTime.now().minusDays(3)
        );
        expired.setAvailableAt(
                LocalDateTime.now().minusDays(2)
        );
        expired.setAvailableUntil(
                LocalDateTime.now().minusHours(1)
        );

        Reservation next =
                pendingReservation(1);

        when(reservationRepository
                .findByStatusAndAvailableUntilBefore(
                        eq(ReservationStatus.AVAILABLE),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(expired));

        when(bookRepository
                .findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .findByIdForUpdate(
                        expiredReservationId
                ))
                .thenReturn(Optional.of(expired));

        /*
         * La reserva anterior ya fue marcada EXPIRED,
         * por lo que ya no consume una copia AVAILABLE.
         */
        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(0L);

        when(reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(
                        bookId,
                        ReservationStatus.PENDING
                ))
                .thenReturn(Optional.of(next));

        when(reservationRepository.findActiveQueue(
                eq(bookId),
                eq(List.of(
                        ReservationStatus.AVAILABLE,
                        ReservationStatus.PENDING
                ))
        )).thenReturn(List.of(next));

        reservationQueueService
                .expireAvailableReservations();

        assertEquals(
                ReservationStatus.EXPIRED,
                expired.getStatus()
        );

        assertEquals(
                ReservationStatus.AVAILABLE,
                next.getStatus()
        );

        assertNotNull(next.getAvailableAt());
        assertNotNull(next.getAvailableUntil());

        assertEquals(
                next.getAvailableAt().plusHours(48),
                next.getAvailableUntil()
        );

        assertEquals(
                1,
                next.getQueuePosition()
        );

        verify(bookRepository)
                .findByIdForUpdate(bookId);

        verify(reservationRepository)
                .findByIdForUpdate(
                        expiredReservationId
                );
    }

    private Reservation pendingReservation(
            long hoursAgo
    ) {

        Reservation reservation =
                new Reservation();

        reservation.setId(UUID.randomUUID());
        reservation.setBook(book);
        reservation.setStatus(
                ReservationStatus.PENDING
        );

        reservation.setReservedAt(
                LocalDateTime.now()
                        .minusHours(hoursAgo)
        );

        reservation.setNotificationSent(false);

        return reservation;
    }
}