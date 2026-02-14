package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
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
import com.pm.librarymanagementsystem.service.BookLoanService;
import com.pm.librarymanagementsystem.service.ReservationService;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final BookLoanService bookLoanService;

    int MAX_RESERVATION = 5;

    @Override
    public ReservationResponse createReservation(ReservationRequest request) {

        User user = userService.getCurrentUserEntity();

        return createReservationForUser(user.getId(), request);
    }

    @Override
    public ReservationResponse createReservationForUser(Long UserId, ReservationRequest request) {
        boolean alreadyHasLoan = bookLoanRepository
                .existsByUserIdAndBookIdAndStatus(UserId, request.bookId(), BookLoanStatus.CHECKED_OUT);
        if(alreadyHasLoan){
            throw new BusinessRuleException("Ya tienes un préstamo sobre este libro");
        }
        User user = userService.findById(UserId);

        Book book = bookRepository.findById(request.bookId()).orElseThrow(
                ()-> new NotFoundException("Libro no encontrado")
        );

        if(reservationRepository.hasActiveReservation(user.getId(), book.getId())){
            throw new BusinessRuleException("Ya tienes una reserva para este libro");
        }

        if(book.getAvailableCopies() > 0){
            throw new BusinessRuleException("El libro ya está disponible.");
        }

        long activeReservation = reservationRepository.countActiveReservationsByUser(user.getId());

        if(activeReservation > MAX_RESERVATION){
            throw new BusinessRuleException("Has reservado "+ MAX_RESERVATION + " veces");
        }

        long pendingCount = reservationRepository.countPendingReservationByBook(book.getId());

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setNotificationSent(false);
        reservation.setNotes(request.notes());
        reservation.setQueuePosition((int)pendingCount + 1);

        return ReservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    public ReservationResponse cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                ()-> new NotFoundException("Reservación no encontrada"));

        User user = userService.getCurrentUserEntity();

        if(!reservation.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN){
            throw new BusinessRuleException("Solo puedes cancelar tu propia reserva");
        }
        if(!reservation.canBeCancelled()){
            throw new BusinessRuleException("La reserva no se puede cancelar (estado actual "+reservation.getStatus()+")");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        return ReservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    public ReservationResponse fulfillReservation(Long reservationId, Integer checkoutDays) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                ()-> new NotFoundException("Reservación no encontrada"));

        if(reservation.getBook().getAvailableCopies() <= 0) {
            throw new BusinessRuleException("No se pueden hacer reservas para recoger (estado actual "+reservation.getStatus()+")");
        }

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservation.setFulfilledAt(LocalDateTime.now());

        BookLoanCheckoutRequest request = new BookLoanCheckoutRequest(
                reservation.getBook().getId(),
                checkoutDays,
                "Reserva realizada por el administrador"
        );

        bookLoanService.checkoutBookForUser(reservation.getUser().getId(),request);

        return ReservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    public PageResponse<ReservationResponse> searchReservations(Long userId, SearchReservationRequest request, Pageable pageable) {

        Page<Reservation> reservations = reservationRepository.searchReservationsWithFilters(
                        userId,
                        request.bookId(),
                        request.status(),
                        request.activityOnly() != null ? request.activityOnly() : false,
                pageable);
        Page<ReservationResponse> mappedPage = reservations.map(ReservationMapper::toResponse);

        return new PageResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast(),
                mappedPage.isFirst(),
                mappedPage.isEmpty()
        );
    }

    @Override
    public PageResponse<ReservationResponse> getMyReservations(SearchReservationRequest request, Pageable pageable) {
        User user = userService.getCurrentUserEntity();

        return searchReservations(user.getId(), request, pageable);
    }
}
