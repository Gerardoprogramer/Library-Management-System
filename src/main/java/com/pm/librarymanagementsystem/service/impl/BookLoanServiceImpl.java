package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.domain.BookLoanType;
import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.BookLoanMapper;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.BookLoan;
import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckinRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckoutRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanRenewalRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoansSearchRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookLoan.BookLoanResponse;
import com.pm.librarymanagementsystem.repository.BookLoanRepository;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.ReservationRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.BookLoanService;
import com.pm.librarymanagementsystem.service.ReservationQueueService;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanService {

    private final BookLoanRepository bookLoanRepository;
    private final SubscriptionService subscriptionService;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReservationQueueService reservationQueueService;
    private final ReservationRepository reservationRepository;

    @Override
    public void checkoutBook(BookLoanCheckoutRequest request) {
        checkoutBookForUser(getCurrentUserId(), request);
    }

    @Transactional
    @Override
    public BookLoanResponse checkoutBookForUser(
            UUID userId,
            BookLoanCheckoutRequest request
    ) {

        User user = userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuario no encontrado"
                        )
                );

        SubscriptionResponse subscription =
                subscriptionService
                        .getActiveSubscriptionForUser(userId);


        Book book = bookRepository
                .findByIdForUpdate(request.bookId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Libro no encontrado"
                        )
                );

        if (!book.getActive()) {
            throw new BusinessRuleException(
                    "El libro no se encuentra activo"
            );
        }

        long reservedCopies =
                reservationRepository
                        .countByBookIdAndStatus(
                                book.getId(),
                                ReservationStatus.AVAILABLE
                        );

        long freelyAvailableCopies =
                book.getAvailableCopies()
                        - reservedCopies;

        if (freelyAvailableCopies <= 0) {
            throw new BusinessRuleException(
                    "No hay copias disponibles fuera de las reservas activas"
            );
        }

        validateCheckoutRules(
                userId,
                book,
                subscription,
                request.checkoutDays()
        );

        LocalDateTime now = LocalDateTime.now();

        BookLoan bookLoan =
                buildBookLoan(
                        user,
                        book,
                        request.checkoutDays(),
                        request.notes(),
                        now
                );

        book.setAvailableCopies(
                book.getAvailableCopies() - 1
        );

        BookLoan savedLoan =
                bookLoanRepository.save(bookLoan);

        return BookLoanMapper.toResponse(
                savedLoan,
                BigDecimal.ZERO
        );
    }

    @Transactional
    @Override
    public BookLoanResponse checkinBook(
            BookLoanCheckinRequest request
    ) {
        UUID userId = getCurrentUserId();

        BookLoan bookLoan = bookLoanRepository
                .findByIdAndUserIdForUpdate(
                        request.loanId(),
                        userId
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Préstamo de libro no encontrado"
                        )
                );

        if (!bookLoan.isActive()) {
            throw new BusinessRuleException(
                    "El préstamo del libro no está activo"
            );
        }

        BookLoanStatus condition =
                request.status() != null
                        ? request.status()
                        : BookLoanStatus.RETURNED;

        if (condition != BookLoanStatus.RETURNED
                && condition != BookLoanStatus.LOST
                && condition != BookLoanStatus.DAMAGED) {

            throw new BusinessRuleException(
                    "Estado de devolución no válido"
            );
        }

        Book book = bookRepository
                .findByIdForUpdate(
                        bookLoan.getBook().getId()
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Libro no encontrado"
                        )
                );

        bookLoan.setReturnDate(LocalDateTime.now());
        bookLoan.setStatus(condition);
        bookLoan.setOverdueDays(0);
        bookLoan.setOverdue(false);
        bookLoan.setNotes(request.notes());

        if (condition == BookLoanStatus.RETURNED) {

            book.setAvailableCopies(
                    book.getAvailableCopies() + 1
            );

            reservationQueueService
                    .promoteNextReservations(book);
        }

        return BookLoanMapper.toResponse(
                bookLoan,
                BigDecimal.ZERO
        );
    }

    @Transactional
    @Override
    public BookLoanResponse renewCheckout(
            BookLoanRenewalRequest request
    ) {

        UUID userId = getCurrentUserId();

        BookLoan bookLoan = bookLoanRepository
                .findByIdAndUserIdForUpdate(
                        request.loanId(),
                        userId
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Préstamo de libro no encontrado"
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        if (!bookLoan.canRenew()
                || !bookLoan.getDueDate().isAfter(now)) {

            throw new BusinessRuleException(
                    "El libro no se puede renovar"
            );
        }

        SubscriptionResponse subscription =
                subscriptionService
                        .getActiveSubscriptionForUser(
                                userId
                        );

        if (request.extensionDays()
                > subscription.maxDaysPerBook()) {

            throw new BusinessRuleException(
                    "La extensión solicitada supera el máximo permitido por tu suscripción"
            );
        }

        bookLoan.setDueDate(
                bookLoan.getDueDate()
                        .plusDays(
                                request.extensionDays()
                        )
        );

        bookLoan.setRenewalCount(
                bookLoan.getRenewalCount() + 1
        );

        bookLoan.setNotes(
                request.notes()
        );

        return BookLoanMapper.toResponse(
                bookLoan,
                BigDecimal.ZERO
        );
    }

    @Override
    public PageResponse<BookLoanResponse> getMyBookLoans(BookLoanStatus status, Pageable pageable) {

        Sort sort = (status != null)
                ? Sort.by("createdAt").descending()
                : Sort.by("dueDate").ascending();

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        BookLoansSearchRequest request = new BookLoansSearchRequest(
        getCurrentUserId(),
                null,
                status,
                false,
                null,
                null,
                null
        );

        return getBookLoans(request, sortedPageable);
    }

    @Override
    public PageResponse<BookLoanResponse> getBookLoans(BookLoansSearchRequest request, Pageable pageable) {

        Page<BookLoanResponse> bookLoanResponsePage = bookLoanRepository.getBookLoans(
                request.userId(),
                Boolean.TRUE.equals(request.overdueOnly()),
                request.bookId(),
                request.status(),
                request.startDate(),
                request.endDate(),
                pageable
        );

        return new PageResponse<>(bookLoanResponsePage.getContent(),
                bookLoanResponsePage.getNumber(),
                bookLoanResponsePage.getSize(),
                bookLoanResponsePage.getTotalElements(),
                bookLoanResponsePage.getTotalPages(),
                bookLoanResponsePage.isLast(),
                bookLoanResponsePage.isFirst(),
                bookLoanResponsePage.isEmpty());
    }

    @Override
    public int updateOverdueBookLoan() {

        final int batchSize = 500;
        final LocalDateTime now = LocalDateTime.now();

        int pageNumber = 0;
        int updatedCount = 0;

        Page<BookLoan> overduePage;

        do {
            Pageable pageable =
                    PageRequest.of(
                            pageNumber,
                            batchSize
                    );

            overduePage =
                    bookLoanRepository
                            .findOverdueBookLoans(
                                    now,
                                    pageable
                            );

            for (BookLoan bookLoan
                    : overduePage.getContent()) {

                boolean changed = false;

                if (bookLoan.getStatus()
                        == BookLoanStatus.CHECKED_OUT) {

                    bookLoan.setStatus(
                            BookLoanStatus.OVERDUE
                    );

                    changed = true;
                }

                if (!bookLoan.isOverdue()) {
                    bookLoan.setOverdue(true);
                    changed = true;
                }

                long calculatedDays =
                        ChronoUnit.DAYS.between(
                                bookLoan
                                        .getDueDate()
                                        .toLocalDate(),
                                now.toLocalDate()
                        );

                int overdueDays =
                        Math.toIntExact(
                                Math.max(
                                        calculatedDays,
                                        0
                                )
                        );

                if (!Objects.equals(
                        bookLoan.getOverdueDays(),
                        overdueDays
                )) {
                    bookLoan.setOverdueDays(
                            overdueDays
                    );

                    changed = true;
                }

                if (changed) {
                    updatedCount++;
                }
            }

            if (!overduePage.isEmpty()) {
                bookLoanRepository.saveAll(
                        overduePage.getContent()
                );
            }

            pageNumber++;

        } while (!overduePage.isLast());

        return updatedCount;
    }

    @Transactional
    @Override
    public BookLoanResponse checkoutReservedBook(
            UUID reservationId,
            Integer checkoutDays
    ) {
        if (checkoutDays == null || checkoutDays < 1) {
            throw new BusinessRuleException(
                    "Los días de préstamo deben ser al menos 1"
            );
        }

        Reservation snapshot = reservationRepository
                .findById(reservationId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Reservación no encontrada"
                        )
                );

        UUID userId =
                snapshot.getUser().getId();

        UUID bookId =
                snapshot.getBook().getId();

        User user = userRepository
                .findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Usuario no encontrado"
                        )
                );

        SubscriptionResponse subscription =
                subscriptionService
                        .getActiveSubscriptionForUser(
                                userId
                        );

        Book book = bookRepository
                .findByIdForUpdate(bookId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Libro no encontrado"
                        )
                );

        Reservation reservation = reservationRepository
                .findByIdForUpdate(reservationId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Reservación no encontrada"
                        )
                );

        if (reservation.getStatus()
                != ReservationStatus.AVAILABLE) {

            throw new BusinessRuleException(
                    "La reserva todavía no está disponible para retiro"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (reservation.getAvailableUntil() != null
                && now.isAfter(
                reservation.getAvailableUntil()
        )) {

            throw new BusinessRuleException(
                    "El período para retirar la reserva ha expirado"
            );
        }

        if (!book.getActive()) {
            throw new BusinessRuleException(
                    "El libro no se encuentra activo"
            );
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException(
                    "El libro no tiene copias disponibles"
            );
        }

        validateCheckoutRules(
                userId,
                book,
                subscription,
                checkoutDays
        );

        BookLoan bookLoan = buildBookLoan(
                user,
                book,
                checkoutDays,
                "Préstamo generado desde una reserva",
                now
        );

        book.setAvailableCopies(
                book.getAvailableCopies() - 1
        );

        reservation.setStatus(
                ReservationStatus.FULFILLED
        );

        reservation.setFulfilledAt(now);

        BookLoan savedLoan =
                bookLoanRepository.save(bookLoan);

        reservationRepository.flush();

        reservationQueueService
                .promoteNextReservations(book);

        return BookLoanMapper.toResponse(
                savedLoan,
                BigDecimal.ZERO
        );
    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void validateCheckoutRules(
            UUID userId,
            Book book,
            SubscriptionResponse subscription,
            Integer checkoutDays
    ) {
        if (bookLoanRepository.hasActiveCheckout(
                userId,
                book.getId()
        )) {
            throw new BusinessRuleException(
                    "El usuario ya tiene un préstamo activo de este libro"
            );
        }

        long activeCheckouts =
                bookLoanRepository
                        .countActiveBookLoansByUser(
                                userId
                        );

        if (activeCheckouts
                >= subscription.maxBooksAllowed()) {

            throw new BusinessRuleException(
                    "Has alcanzado el número máximo de libros permitido"
            );
        }

        long overdueCount =
                bookLoanRepository
                        .countCurrentlyOverdueBookLoansByUser(
                                userId,
                                LocalDateTime.now()
                        );

        if (overdueCount > 0) {
            throw new BusinessRuleException(
                    "Debes devolver los préstamos vencidos antes de solicitar otro libro"
            );
        }

        if (checkoutDays
                > subscription.maxDaysPerBook()) {

            throw new BusinessRuleException(
                    "El período solicitado supera el máximo permitido por tu suscripción"
            );
        }
    }

    private BookLoan buildBookLoan(
            User user,
            Book book,
            Integer checkoutDays,
            String notes,
            LocalDateTime now
    ) {
        return BookLoan.builder()
                .user(user)
                .book(book)
                .type(BookLoanType.CHECKOUT)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkoutDate(now)
                .dueDate(
                        now.plusDays(checkoutDays)
                )
                .renewalCount(0)
                .maxRenewals(2)
                .notes(notes)
                .overdue(false)
                .overdueDays(0)
                .build();
    }
}
