package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.*;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookLoan.BookLoanResponse;
import org.springframework.data.domain.Pageable;

public interface BookLoanService {

    BookLoanResponse checkoutBook(BookLoanCheckoutRequest request);

    BookLoanResponse checkoutBookForUser(Long userId, BookLoanCheckoutRequest request);

    BookLoanResponse checkinBook(BookLoanCheckinRequest request);

    BookLoanResponse renewCheckout(BookLoanRenewalRequest request);

    PageResponse<BookLoanResponse> getMyBookLoans(BookLoanStatus status, Pageable pageable);

    PageResponse<BookLoanResponse> getBookLoans(BookLoansSearchRequest request, Pageable pageable);

    int updateOverdueBookLoan();


}
