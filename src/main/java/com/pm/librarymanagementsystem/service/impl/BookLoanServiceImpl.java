package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.domain.BookLoanType;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.BookLoanMapper;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.BookLoan;
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
import com.pm.librarymanagementsystem.service.BookLoanService;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import com.pm.librarymanagementsystem.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanService {

    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final BookRepository bookRepository;

    @Override
    public BookLoanResponse checkoutBook(BookLoanCheckoutRequest request) {

        User user = userService.getCurrentUserEntity();

        return checkoutBookForUser(user.getId(), request);
    }

    @Transactional
    @Override
    public BookLoanResponse checkoutBookForUser(UUID userId, BookLoanCheckoutRequest request) {
        User user = userService.findById(userId);

        SubscriptionResponse subscription = subscriptionService.getUsersActiveSubscription();
        Book book = bookRepository.findById(request.bookId()).orElseThrow(
                ()-> new NotFoundException("No se encontro el Libro")
        );

        if(!book.getActive()){
            throw new BusinessRuleException("El libro no se encuentra activo");
        }

        if(book.getAvailableCopies() <= 0){
            throw new BusinessRuleException("El libro no está disponible.");
        }

        if(bookLoanRepository.hasActiveCheckout(user.getId(), book.getId())){
            throw new BusinessRuleException("El libro ya tiene un proceso de pago activo.");
        }

        Long activeCheckouts = bookLoanRepository.countActiveBookLoansByUser(user.getId());

        int maxBookAllowed = subscription.maxBooksAllowed();
        if(activeCheckouts >= maxBookAllowed){
            throw new BusinessRuleException("Has alcanzado el número máximo de libros permitido.");
        }

        long overdueCount = bookLoanRepository.countOverdueBookLoansByUser(user.getId());

        if(overdueCount > 0){
            throw new BusinessRuleException("Primero devuelve el libro viejo.");
        }

        BookLoan bookLoan = BookLoan
                .builder()
                .user(user)
                .book(book)
                .type(BookLoanType.CHECKOUT)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkoutDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(request.checkoutDays()))
                .renewalCount(0)
                // despues hacer configurable no Hardcode
                .maxRenewals(2)
                .notes(request.notes())
                .overdue(false)
                .overdueDays(0)
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return BookLoanMapper.toResponse(bookLoanRepository.save(bookLoan));
    }

    @Transactional
    @Override
    public BookLoanResponse checkinBook(BookLoanCheckinRequest request) {

        BookLoan bookLoan = bookLoanRepository.findById(request.loanId())
                .orElseThrow(()-> new NotFoundException("Préstamo de libro no encontrado"));

        if(!bookLoan.isActive()){
            throw new BusinessRuleException("El préstamo del libro no está activo.");
        }

        bookLoan.setReturnDate(LocalDateTime.now());

        BookLoanStatus condition = request.status();

        if(condition == null){
            condition = BookLoanStatus.RETURNED;
        }
        bookLoan.setStatus(condition);
        bookLoan.setOverdueDays(0);
        bookLoan.setOverdue(false);
        bookLoan.setNotes(request.notes());

        if(condition != BookLoanStatus.LOST){
            Book book = bookLoan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }

        return BookLoanMapper.toResponse(bookLoanRepository.save(bookLoan));
    }

    @Transactional
    @Override
    public BookLoanResponse renewCheckout(BookLoanRenewalRequest request) {

        BookLoan bookLoan = bookLoanRepository.findById(request.loanId())
                .orElseThrow(()-> new NotFoundException("Préstamo de libro no encontrado"));

        if(!bookLoan.canRenew()){
            throw new BusinessRuleException("El libro no se puede renovar");
        }

        bookLoan.setDueDate(bookLoan.getDueDate().plusDays(request.extensionDays()));
        bookLoan.setRenewalCount(bookLoan.getRenewalCount() + 1);
        bookLoan.setNotes(request.notes());

        return BookLoanMapper.toResponse(bookLoanRepository.save(bookLoan));
    }

    @Override
    public PageResponse<BookLoanResponse> getMyBookLoans(BookLoanStatus status, Pageable pageable) {

        User user = userService.getCurrentUserEntity();
        Page<BookLoan> response;

        Sort sort = (status != null)
                ? Sort.by("createdAt").descending()
                : Sort.by("dueDate").ascending();

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        if (status != null) {
            response = bookLoanRepository.findByStatusAndUser(user, status, sortedPageable);
        } else {
            response = bookLoanRepository.findByUserId(user.getId(), sortedPageable);
        }

        Page<BookLoanResponse> bookLoanResponsePage = response.map(BookLoanMapper::toResponse);

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
    public PageResponse<BookLoanResponse> getBookLoans(BookLoansSearchRequest request, Pageable pageable) {

        Page<BookLoan> response;

        if(Boolean.TRUE.equals(request.overdueOnly())){
            response = bookLoanRepository.findOverdueBookLoans(LocalDateTime.now(), pageable);
        }else if(request.userId() != null){
            response = bookLoanRepository.findByUserId(request.userId(), pageable);
        }else if(request.bookId() != null){
            response = bookLoanRepository.findByBookId(request.bookId(), pageable);
        }else if(request.status() != null){
            response = bookLoanRepository.findByStatus(request.status(), pageable);
        }else if(request.startDate() != null && request.endDate() != null){
            response = bookLoanRepository.findBookLoansByDateRange(
                    request.startDate(), request.endDate(), pageable
            );
        }else {
            response = bookLoanRepository.findAll(pageable);
        }

        Page<BookLoanResponse> bookLoanResponsePage = response.map(BookLoanMapper::toResponse);

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
        Pageable pageable = PageRequest.of(0, 1000);
        Page<BookLoan> overduePage = bookLoanRepository
                .findOverdueBookLoans(LocalDateTime.now(), pageable);

        int updateCount = 0;
        for(BookLoan bookLoan: overduePage.getContent()){
            if(bookLoan.getStatus() == BookLoanStatus.CHECKED_OUT){
                bookLoan.setStatus(BookLoanStatus.OVERDUE);
                bookLoan.setOverdue(true);

                bookLoanRepository.save(bookLoan);
                updateCount++;
            }
        }

        return updateCount;
    }
}
