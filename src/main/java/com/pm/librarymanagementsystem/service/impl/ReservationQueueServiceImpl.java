package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.ReservationRepository;
import com.pm.librarymanagementsystem.service.ReservationQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationQueueServiceImpl
        implements ReservationQueueService {

    private static final int PICKUP_HOURS = 48;

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void promoteNextReservations(
            Book book
    ) {
        long reservedCopies =
                reservationRepository
                        .countByBookIdAndStatus(
                                book.getId(),
                                ReservationStatus.AVAILABLE
                        );

        long freeCopies =
                book.getAvailableCopies()
                        - reservedCopies;

        while (freeCopies > 0) {

            Reservation next =
                    reservationRepository
                            .findFirstByBookIdAndStatusOrderByReservedAtAsc(
                                    book.getId(),
                                    ReservationStatus.PENDING
                            )
                            .orElse(null);

            if (next == null) {
                break;
            }

            LocalDateTime now =
                    LocalDateTime.now();

            next.setStatus(
                    ReservationStatus.AVAILABLE
            );

            next.setAvailableAt(now);

            next.setAvailableUntil(
                    now.plusHours(PICKUP_HOURS)
            );

            next.setNotificationSent(false);

            freeCopies--;
        }

        recalculateQueuePositions(
                book.getId()
        );
    }

    @Override
    @Transactional
    public void expireAvailableReservations() {

        LocalDateTime now =
                LocalDateTime.now();

        List<Reservation> expired =
                reservationRepository
                        .findByStatusAndAvailableUntilBefore(
                                ReservationStatus.AVAILABLE,
                                now
                        );

        for (Reservation candidate : expired) {

            Book book = bookRepository
                    .findByIdForUpdate(
                            candidate
                                    .getBook()
                                    .getId()
                    )
                    .orElse(null);

            if (book == null) {
                continue;
            }

            Reservation reservation =
                    reservationRepository
                            .findByIdForUpdate(
                                    candidate.getId()
                            )
                            .orElse(null);

            if (reservation == null
                    || reservation.getStatus()
                    != ReservationStatus.AVAILABLE
                    || reservation.getAvailableUntil() == null
                    || !reservation
                    .getAvailableUntil()
                    .isBefore(now)) {

                continue;
            }

            reservation.setStatus(
                    ReservationStatus.EXPIRED
            );

            /*
             * Esa copia nunca se descontó de availableCopies;
             * simplemente estaba apartada.
             * Al expirar, podemos ofrecerla al siguiente.
             */
            promoteNextReservations(book);
        }
    }

    private void recalculateQueuePositions(
            UUID bookId
    ) {
        List<Reservation> activeQueue =
                reservationRepository
                        .findActiveQueue(
                                bookId,
                                List.of(
                                        ReservationStatus.AVAILABLE,
                                        ReservationStatus.PENDING
                                )
                        );

        int position = 1;

        for (Reservation reservation : activeQueue) {
            reservation.setQueuePosition(
                    position++
            );
        }
    }
}