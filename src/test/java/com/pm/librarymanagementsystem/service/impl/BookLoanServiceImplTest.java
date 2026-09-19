package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.domain.ReservationStatus;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.BookLoan;
import com.pm.librarymanagementsystem.modal.Reservation;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckinRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckoutRequest;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.repository.BookLoanRepository;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.ReservationRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.ReservationQueueService;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookLoanServiceImplTest {

    @Mock
    private BookLoanRepository bookLoanRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationQueueService reservationQueueService;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private BookLoanServiceImpl bookLoanService;

    private UUID userId;
    private UUID bookId;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setFullName("Gerardo Martínez");

        book = new Book();
        book.setId(bookId);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setActive(true);
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
    }

    @Test
    void checkoutBookForUser_shouldCreateLoanAndDecreaseAvailableCopies() {

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(subscription.maxBooksAllowed())
                .thenReturn(5);

        when(subscription.maxDaysPerBook())
                .thenReturn(14);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(1L);

        when(bookLoanRepository.hasActiveCheckout(
                userId,
                bookId
        )).thenReturn(false);

        when(bookLoanRepository
                .countActiveBookLoansByUser(userId))
                .thenReturn(1L);

        when(bookLoanRepository
                .countCurrentlyOverdueBookLoansByUser(
                        eq(userId),
                        any()
                ))
                .thenReturn(0L);

        when(bookLoanRepository.save(any(BookLoan.class)))
                .thenAnswer(invocation -> {

                    BookLoan loan =
                            invocation.getArgument(0);

                    loan.setId(UUID.randomUUID());

                    return loan;
                });

        BookLoanCheckoutRequest request =
                new BookLoanCheckoutRequest(
                        bookId,
                        7,
                        "Préstamo de prueba"
                );

        var response =
                bookLoanService.checkoutBookForUser(
                        userId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                BookLoanStatus.CHECKED_OUT,
                response.status()
        );

        assertEquals(
                bookId,
                response.bookId()
        );

        assertEquals(
                userId,
                response.userId()
        );

        assertEquals(
                2,
                book.getAvailableCopies()
        );

        ArgumentCaptor<BookLoan> loanCaptor =
                ArgumentCaptor.forClass(BookLoan.class);

        verify(bookLoanRepository)
                .save(loanCaptor.capture());

        BookLoan savedLoan =
                loanCaptor.getValue();

        assertEquals(user, savedLoan.getUser());
        assertEquals(book, savedLoan.getBook());

        assertEquals(
                BookLoanStatus.CHECKED_OUT,
                savedLoan.getStatus()
        );

        assertEquals(
                "Préstamo de prueba",
                savedLoan.getNotes()
        );

        assertEquals(
                0,
                savedLoan.getRenewalCount()
        );

        assertEquals(
                2,
                savedLoan.getMaxRenewals()
        );

        assertFalse(savedLoan.isOverdue());

        assertNotNull(savedLoan.getCheckoutDate());
        assertNotNull(savedLoan.getDueDate());
    }

    @Test
    void checkoutBookForUser_shouldNotUseCopiesReservedForOtherUsers() {

        book.setAvailableCopies(2);

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(2L);

        BookLoanCheckoutRequest request =
                new BookLoanCheckoutRequest(
                        bookId,
                        7,
                        null
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () ->
                                bookLoanService
                                        .checkoutBookForUser(
                                                userId,
                                                request
                                        )
                );

        assertEquals(
                "No hay copias disponibles fuera de las reservas activas",
                exception.getMessage()
        );

        assertEquals(
                2,
                book.getAvailableCopies()
        );

        verify(bookLoanRepository, never())
                .save(any());
    }

    @Test
    void checkoutBookForUser_shouldRejectWhenSubscriptionBookLimitIsReached() {

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(subscription.maxBooksAllowed())
                .thenReturn(2);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(0L);

        when(bookLoanRepository.hasActiveCheckout(
                userId,
                bookId
        )).thenReturn(false);

        when(bookLoanRepository
                .countActiveBookLoansByUser(userId))
                .thenReturn(2L);

        BookLoanCheckoutRequest request =
                new BookLoanCheckoutRequest(
                        bookId,
                        7,
                        null
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () ->
                                bookLoanService
                                        .checkoutBookForUser(
                                                userId,
                                                request
                                        )
                );

        assertEquals(
                "Has alcanzado el número máximo de libros permitido",
                exception.getMessage()
        );

        assertEquals(
                3,
                book.getAvailableCopies()
        );

        verify(bookLoanRepository, never())
                .save(any());
    }

    @Test
    void checkoutBookForUser_shouldRejectCheckoutDaysAboveSubscriptionLimit() {

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(subscription.maxBooksAllowed())
                .thenReturn(5);

        when(subscription.maxDaysPerBook())
                .thenReturn(14);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(0L);

        when(bookLoanRepository.hasActiveCheckout(
                userId,
                bookId
        )).thenReturn(false);

        when(bookLoanRepository
                .countActiveBookLoansByUser(userId))
                .thenReturn(1L);

        when(bookLoanRepository
                .countCurrentlyOverdueBookLoansByUser(
                        eq(userId),
                        any()
                ))
                .thenReturn(0L);

        BookLoanCheckoutRequest request =
                new BookLoanCheckoutRequest(
                        bookId,
                        20,
                        null
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () ->
                                bookLoanService
                                        .checkoutBookForUser(
                                                userId,
                                                request
                                        )
                );

        assertEquals(
                "El período solicitado supera el máximo permitido por tu suscripción",
                exception.getMessage()
        );

        assertEquals(
                3,
                book.getAvailableCopies()
        );

        verify(bookLoanRepository, never())
                .save(any());
    }

    @Test
    void checkoutBookForUser_shouldRejectDuplicateActiveCheckout() {

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(0L);

        when(bookLoanRepository.hasActiveCheckout(
                userId,
                bookId
        )).thenReturn(true);

        BookLoanCheckoutRequest request =
                new BookLoanCheckoutRequest(
                        bookId,
                        7,
                        null
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> bookLoanService
                                .checkoutBookForUser(
                                        userId,
                                        request
                                )
                );

        assertEquals(
                "El usuario ya tiene un préstamo activo de este libro",
                exception.getMessage()
        );

        assertEquals(
                3,
                book.getAvailableCopies()
        );

        verify(bookLoanRepository, never())
                .save(any());
    }

    @Test
    void checkoutBookForUser_shouldRejectUserWithOverdueLoans() {

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(subscription.maxBooksAllowed())
                .thenReturn(5);

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .countByBookIdAndStatus(
                        bookId,
                        ReservationStatus.AVAILABLE
                ))
                .thenReturn(0L);

        when(bookLoanRepository.hasActiveCheckout(
                userId,
                bookId
        )).thenReturn(false);

        when(bookLoanRepository
                .countActiveBookLoansByUser(userId))
                .thenReturn(1L);

        when(bookLoanRepository
                .countCurrentlyOverdueBookLoansByUser(
                        eq(userId),
                        any(LocalDateTime.class)
                ))
                .thenReturn(1L);

        BookLoanCheckoutRequest request =
                new BookLoanCheckoutRequest(
                        bookId,
                        7,
                        null
                );

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> bookLoanService
                                .checkoutBookForUser(
                                        userId,
                                        request
                                )
                );

        assertEquals(
                "Debes devolver los préstamos vencidos antes de solicitar otro libro",
                exception.getMessage()
        );

        assertEquals(
                3,
                book.getAvailableCopies()
        );

        verify(bookLoanRepository, never())
                .save(any());
    }

    @Test
    void checkoutReservedBook_shouldRejectPendingReservation() {

        UUID reservationId = UUID.randomUUID();

        Reservation reservation =
                new Reservation();

        reservation.setId(reservationId);
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(
                ReservationStatus.PENDING
        );

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .findByIdForUpdate(reservationId))
                .thenReturn(Optional.of(reservation));

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> bookLoanService
                                .checkoutReservedBook(
                                        reservationId,
                                        7
                                )
                );

        assertEquals(
                "La reserva todavía no está disponible para retiro",
                exception.getMessage()
        );

        assertEquals(
                ReservationStatus.PENDING,
                reservation.getStatus()
        );

        assertEquals(
                3,
                book.getAvailableCopies()
        );

        verify(bookLoanRepository, never())
                .save(any());

        verify(reservationQueueService, never())
                .promoteNextReservations(any());
    }

    @Test
    void checkoutReservedBook_shouldFulfillAvailableReservation() {

        UUID reservationId = UUID.randomUUID();

        Reservation reservation =
                new Reservation();

        reservation.setId(reservationId);
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(
                ReservationStatus.AVAILABLE
        );

        reservation.setAvailableAt(
                LocalDateTime.now().minusHours(1)
        );

        reservation.setAvailableUntil(
                LocalDateTime.now().plusHours(24)
        );

        SubscriptionResponse subscription =
                mock(SubscriptionResponse.class);

        when(subscription.maxBooksAllowed())
                .thenReturn(5);

        when(subscription.maxDaysPerBook())
                .thenReturn(14);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        when(userRepository.findByIdForUpdate(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionService
                .getActiveSubscriptionForUser(userId))
                .thenReturn(subscription);

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        when(reservationRepository
                .findByIdForUpdate(reservationId))
                .thenReturn(Optional.of(reservation));

        when(bookLoanRepository.hasActiveCheckout(
                userId,
                bookId
        )).thenReturn(false);

        when(bookLoanRepository
                .countActiveBookLoansByUser(userId))
                .thenReturn(1L);

        when(bookLoanRepository
                .countCurrentlyOverdueBookLoansByUser(
                        eq(userId),
                        any(LocalDateTime.class)
                ))
                .thenReturn(0L);

        when(bookLoanRepository.save(
                any(BookLoan.class)
        )).thenAnswer(invocation -> {

            BookLoan loan =
                    invocation.getArgument(0);

            loan.setId(UUID.randomUUID());

            return loan;
        });

        var response =
                bookLoanService.checkoutReservedBook(
                        reservationId,
                        7
                );

        assertNotNull(response);

        assertEquals(
                BookLoanStatus.CHECKED_OUT,
                response.status()
        );

        assertEquals(
                ReservationStatus.FULFILLED,
                reservation.getStatus()
        );

        assertNotNull(
                reservation.getFulfilledAt()
        );

        assertEquals(
                2,
                book.getAvailableCopies()
        );

        ArgumentCaptor<BookLoan> loanCaptor =
                ArgumentCaptor.forClass(
                        BookLoan.class
                );

        verify(bookLoanRepository)
                .save(loanCaptor.capture());

        BookLoan savedLoan =
                loanCaptor.getValue();

        assertEquals(
                user,
                savedLoan.getUser()
        );

        assertEquals(
                book,
                savedLoan.getBook()
        );

        assertEquals(
                BookLoanStatus.CHECKED_OUT,
                savedLoan.getStatus()
        );

        assertEquals(
                "Préstamo generado desde una reserva",
                savedLoan.getNotes()
        );

        verify(reservationRepository)
                .flush();

        verify(reservationQueueService)
                .promoteNextReservations(book);
    }

    @Test
    void checkinBook_shouldReturnCopyAndPromoteReservations() {

        UUID loanId = UUID.randomUUID();

        BookLoan loan = BookLoan.builder()
                .id(loanId)
                .user(user)
                .book(book)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkoutDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(2))
                .renewalCount(0)
                .maxRenewals(2)
                .overdue(false)
                .overdueDays(0)
                .build();

        book.setAvailableCopies(2);

        when(bookLoanRepository.findByIdForUpdate(loanId))
                .thenReturn(Optional.of(loan));

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        BookLoanCheckinRequest request =
                new BookLoanCheckinRequest(
                        loanId,
                        BookLoanStatus.RETURNED,
                        "Devuelto en buen estado"
                );

        var response =
                bookLoanService.checkinBook(request);

        assertEquals(
                BookLoanStatus.RETURNED,
                loan.getStatus()
        );

        assertNotNull(
                loan.getReturnDate()
        );

        assertFalse(
                loan.isOverdue()
        );

        assertEquals(
                0,
                loan.getOverdueDays()
        );

        assertEquals(
                "Devuelto en buen estado",
                loan.getNotes()
        );

        assertEquals(
                3,
                book.getAvailableCopies()
        );

        assertEquals(
                BookLoanStatus.RETURNED,
                response.status()
        );

        verify(reservationQueueService)
                .promoteNextReservations(book);
    }

    @Test
    void checkinBook_shouldNotIncreaseAvailableCopiesWhenBookIsLost() {

        UUID loanId = UUID.randomUUID();

        BookLoan loan = BookLoan.builder()
                .id(loanId)
                .user(user)
                .book(book)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkoutDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(2))
                .renewalCount(0)
                .maxRenewals(2)
                .overdue(false)
                .overdueDays(0)
                .build();

        book.setAvailableCopies(2);

        when(bookLoanRepository.findByIdForUpdate(loanId))
                .thenReturn(Optional.of(loan));

        when(bookRepository.findByIdForUpdate(bookId))
                .thenReturn(Optional.of(book));

        BookLoanCheckinRequest request =
                new BookLoanCheckinRequest(
                        loanId,
                        BookLoanStatus.LOST,
                        "Libro reportado como perdido"
                );

        var response =
                bookLoanService.checkinBook(request);

        assertEquals(
                BookLoanStatus.LOST,
                loan.getStatus()
        );

        assertNotNull(
                loan.getReturnDate()
        );

        assertEquals(
                2,
                book.getAvailableCopies()
        );

        assertEquals(
                BookLoanStatus.LOST,
                response.status()
        );

        verify(reservationQueueService, never())
                .promoteNextReservations(any());
    }


}