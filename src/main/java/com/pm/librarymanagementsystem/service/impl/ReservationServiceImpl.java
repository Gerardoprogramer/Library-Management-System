package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.domain.UserRole;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.ReservationMapper;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckoutRequest;
import com.pm.librarymanagementsystem.payload.dto.request.reservation.ReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.request.reservation.SearchReservationRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.reservation.ReservationResponse;
import com.pm.librarymanagementsystem.repository.BookLoanRepository;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.ReservationRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.BookLoanService;
import com.pm.librarymanagementsystem.service.ReservationQueueService;
import com.pm.librarymanagementsystem.service.ReservationService;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final BookLoanService bookLoanService;
    private final UserRepository userRepository;
    private final ReservationQueueService reservationQueueService;

    private static final int MAX_RESERVATIONS = 5;
    private static final int RESERVATION_PICKUP_HOURS = 48;

    @Override
    @Transactional
    public ReservationResponse createReservation(
            ReservationRequest request
    ) {
        return createReservationForUser(
                getCurrentUserId(),
                request
        );
    }

    @Override
    @Transactional
    public ReservationResponse createReservationForUser(
            UUID userId,
            ReservationRequest request
    ) {
        User user = userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuario no encontrado"
                        )
                );

        Book book = bookRepository
                .findByIdForUpdate(request.bookId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Libro no encontrado"
                        )
                );

        if (bookLoanRepository.hasActiveCheckout(
                userId,
                book.getId()
        )) {
            throw new BusinessRuleException(
                    "Ya tienes un préstamo activo sobre este libro"
            );
        }

        if (reservationRepository.hasActiveReservation(
                userId,
                book.getId()
        )) {
            throw new BusinessRuleException(
                    "Ya tienes una reserva activa para este libro"
            );
        }

        if (book.getAvailableCopies() > 0) {
            throw new BusinessRuleException(
                    "El libro ya está disponible"
            );
        }

        long activeReservations =
                reservationRepository
                        .countActiveReservationsByUser(userId);

        if (activeReservations >= MAX_RESERVATIONS) {
            throw new BusinessRuleException(
                    "Has alcanzado el máximo de "
                            + MAX_RESERVATIONS
                            + " reservas activas"
            );
        }

        long pendingCount =
                reservationRepository
                        .countPendingReservationByBook(
                                book.getId()
                        );

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setNotificationSent(false);
        reservation.setNotes(request.notes());
        reservation.setQueuePosition(
                Math.toIntExact(pendingCount + 1)
        );

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return ReservationMapper.toResponse(
                savedReservation
        );
    }

    @Transactional
    @Override
    public ReservationResponse cancelReservation(
            UUID reservationId
    ) {
        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Reservación no encontrada"
                        )
                );

        boolean wasAvailable =
                reservation.getStatus()
                        == ReservationStatus.AVAILABLE;

        if (wasAvailable) {

            Book book = bookRepository
                    .findByIdForUpdate(
                            reservation
                                    .getBook()
                                    .getId()
                    )
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Libro no encontrado"
                            )
                    );

            reservationQueueService
                    .promoteNextReservations(book);
        }

        User user = userService.getCurrentUserEntity();

        if (!reservation.getUser()
                .getId()
                .equals(user.getId())
                && user.getRole() != UserRole.ROLE_ADMIN) {

            throw new BusinessRuleException(
                    "Solo puedes cancelar tu propia reserva"
            );
        }

        if (!reservation.canBeCancelled()) {
            throw new BusinessRuleException(
                    "La reserva no se puede cancelar en estado "
                            + reservation.getStatus()
            );
        }

        reservation.setStatus(
                ReservationStatus.CANCELLED
        );

        reservation.setCancelledAt(
                LocalDateTime.now()
        );

        return ReservationMapper.toResponse(
                reservation
        );
    }

    @Transactional
    @Override
    public ReservationResponse fulfillReservation(
            UUID reservationId,
            Integer checkoutDays
    ) {
        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Reservación no encontrada"
                        )
                );

        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.AVAILABLE) {

            throw new BusinessRuleException(
                    "La reserva no puede completarse en estado "
                            + reservation.getStatus()
            );
        }

        if (checkoutDays == null || checkoutDays < 1) {
            throw new BusinessRuleException(
                    "Los días de préstamo deben ser al menos 1"
            );
        }

        BookLoanCheckoutRequest checkoutRequest =
                new BookLoanCheckoutRequest(
                        reservation.getBook().getId(),
                        checkoutDays,
                        "Préstamo generado desde una reserva"
                );

        bookLoanService.checkoutBookForUser(
                reservation.getUser().getId(),
                checkoutRequest
        );

        reservation.setStatus(
                ReservationStatus.FULFILLED
        );

        reservation.setFulfilledAt(
                LocalDateTime.now()
        );

        return ReservationMapper.toResponse(
                reservation
        );
    }

    @Override
    public PageResponse<ReservationResponse> searchReservations(UUID userId, SearchReservationRequest request, Pageable pageable) {

        Page<ReservationResponse> reservations = reservationRepository.searchReservationsWithFilters(
                        userId,
                        request.bookId(),
                        request.status(),
                        request.activityOnly() != null ? request.activityOnly() : false,
                pageable);

        return new PageResponse<>(
                reservations.getContent(),
                reservations.getNumber(),
                reservations.getSize(),
                reservations.getTotalElements(),
                reservations.getTotalPages(),
                reservations.isLast(),
                reservations.isFirst(),
                reservations.isEmpty()
        );
    }

    @Override
    public PageResponse<ReservationResponse> getMyReservations(SearchReservationRequest request, Pageable pageable) {

        return searchReservations(getCurrentUserId(), request, pageable);
    }

    @Override
    public Long positionUserForBook(UUID bookId) {
        return reservationRepository.countPendingReservationByBook(bookId);
    }

    @Override
    @Transactional
    public void promoteNextReservations(Book book) {

        long availableReservations =
                reservationRepository.countByBookIdAndStatus(
                        book.getId(),
                        ReservationStatus.AVAILABLE
                );

        long freeCopies =
                book.getAvailableCopies()
                        - availableReservations;

        while (freeCopies > 0) {

            Reservation nextReservation =
                    reservationRepository
                            .findFirstByBookIdAndStatusOrderByReservedAtAsc(
                                    book.getId(),
                                    ReservationStatus.PENDING
                            )
                            .orElse(null);

            if (nextReservation == null) {
                break;
            }

            LocalDateTime now = LocalDateTime.now();

            nextReservation.setStatus(
                    ReservationStatus.AVAILABLE
            );

            nextReservation.setAvailableAt(now);

            nextReservation.setAvailableUntil(
                    now.plusHours(
                            RESERVATION_PICKUP_HOURS
                    )
            );

            nextReservation.setNotificationSent(false);

            freeCopies--;
        }

        recalculateQueuePositions(
                book.getId()
        );
    }

    @Override
    public void expireAvailableReservations() {

    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void recalculateQueuePositions(
            UUID bookId
    ) {
        List<Reservation> activeQueue =
                reservationRepository.findActiveQueue(
                        bookId,
                        List.of(
                                ReservationStatus.AVAILABLE,
                                ReservationStatus.PENDING
                        )
                );

        int position = 1;

        for (Reservation reservation : activeQueue) {
            reservation.setQueuePosition(position++);
        }
    }
}
