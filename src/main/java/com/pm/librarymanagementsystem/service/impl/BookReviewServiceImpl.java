package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.BookReviewMapper;
import com.pm.librarymanagementsystem.modal.Book;
import com.pm.librarymanagementsystem.modal.BookLoan;
import com.pm.librarymanagementsystem.modal.BookReview;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.CreateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookReview.UpdateReviewRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookReview.BookReviewResponse;
import com.pm.librarymanagementsystem.repository.BookLoanRepository;
import com.pm.librarymanagementsystem.repository.BookRepository;
import com.pm.librarymanagementsystem.repository.BookReviewRepository;
import com.pm.librarymanagementsystem.service.BookReviewService;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;

    @Override
    public BookReviewResponse createReview(CreateReviewRequest request) {

        User user = userService.getCurrentUserEntity();

        Book book = bookRepository.findById(request.bookId()).orElseThrow(
                ()-> new NotFoundException("Libro no encontrado"));

        if(bookReviewRepository.existsByUserIdAndBookId(user.getId(), book.getId())){
            throw new BusinessRuleException("Ya existe una reseña del libro");
        }

        boolean hasReadBook = hasUserReadBook(user.getId(), book.getId());
        if(!hasReadBook){
            throw new BusinessRuleException("No has leído este libro");
        }

        BookReview bookReview  = new BookReview();
        bookReview.setUser(user);
        bookReview.setBook(book);
        bookReview.setRating(request.rating());
        bookReview.setReviewText(request.reviewText());
        bookReview.setTitle(request.title());

        return BookReviewMapper.toResponse(bookReviewRepository.save(bookReview));
    }

    @Override
    public BookReviewResponse updateReview(Long reviewId, UpdateReviewRequest updateReviewRequest) {
        User user = userService.getCurrentUserEntity();

        BookReview bookReview = bookReviewRepository.findById(reviewId).orElseThrow(
                ()-> new NotFoundException("Reseña no encontrada"));

        if(!bookReview.getUser().getId().equals(user.getId())){
            throw new BusinessRuleException("No has reseñado este libro");
        }

        bookReview.setReviewText(updateReviewRequest.reviewText());
        bookReview.setTitle(updateReviewRequest.title());
        bookReview.setRating(updateReviewRequest.rating());

        return BookReviewMapper.toResponse(bookReviewRepository.save(bookReview));
    }

    @Override
    public void deleteReview(Long reviewId) {
        User user = userService.getCurrentUserEntity();

        BookReview bookReview = bookReviewRepository.findById(reviewId).orElseThrow(
                ()-> new NotFoundException("Reseña no encontrada"));

        if(!bookReview.getUser().getId().equals(user.getId())){
            throw new BusinessRuleException("Solo puedes eliminar tus propias reseñas");
        }

        bookReviewRepository.delete(bookReview);
    }

    @Override
    public PageResponse<BookReviewResponse> getReviewsByBookId(Long bookId, Pageable pageable) {

        Book book = bookRepository.findById(bookId).orElseThrow(
                ()-> new NotFoundException("Libro no encontrado")
        );

        Page<BookReview> bookReviewPage = bookReviewRepository.findByBook(book, pageable);
        Page<BookReviewResponse> mappedPage = bookReviewPage.map(BookReviewMapper::toResponse);

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

    private boolean hasUserReadBook(Long userId, Long bookId) {
        List<BookLoan> bookLoanList = bookLoanRepository.findByBookId(bookId);

        return bookLoanList.stream().anyMatch(loan -> loan.getUser().getId().equals(userId)
        && loan.getStatus() == BookLoanStatus.RETURNED);
    }
}
