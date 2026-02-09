package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.domain.BookLoanStatus;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckinRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckoutRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanRenewalRequest;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoansSearchRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.bookLoan.BookLoanResponse;
import com.pm.librarymanagementsystem.service.BookLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-loans")
public class BookLoanController {

    private final BookLoanService bookLoanService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<BookLoanResponse>> checkoutBook(
            @Valid @RequestBody BookLoanCheckoutRequest request
            ){

            return ResponseEntity.ok(ApiResponse.success(
                    "Préstamo creado correctamente",
                    bookLoanService.checkoutBook(request)
            ));
    }

    @PostMapping("/admin/users/{userId}/checkout")
    public ResponseEntity<ApiResponse<BookLoanResponse>> checkoutForUser(
               @PathVariable Long userId,
               @Valid @RequestBody BookLoanCheckoutRequest request
        ){

        return ResponseEntity.ok(ApiResponse.success(
                "Préstamo creado correctamente",
                bookLoanService.checkoutBookForUser(userId, request)
        ));
    }

    @PostMapping("/checkin")
    public ResponseEntity<ApiResponse<BookLoanResponse>> checkinBook(
            @Valid @RequestBody BookLoanCheckinRequest request
    ){

        return ResponseEntity.ok(ApiResponse.success(
                "Libro devuelto correctamente",
                bookLoanService.checkinBook(request)
        ));
    }

    @PostMapping("/renew")
    public ResponseEntity<ApiResponse<BookLoanResponse>> renewCheckout(
            @Valid @RequestBody BookLoanRenewalRequest request
    ){

        return ResponseEntity.ok(ApiResponse.success(
                "Préstamo renovado correctamente",
                bookLoanService.renewCheckout(request)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<BookLoanResponse>>> getMyBookLoans(
            @RequestParam(required = false) BookLoanStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){

        return ResponseEntity.ok(ApiResponse.success(
                "Préstamos obtenidos correctamente",
                bookLoanService.getMyBookLoans(status, pageable)
        ));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<BookLoanResponse>>> getAllBookLoans(
            @RequestBody BookLoansSearchRequest request,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){

        return ResponseEntity.ok(ApiResponse.success(
              "Préstamos obtenidos correctamente",
                bookLoanService.getBookLoans(request, pageable)
        ));
    }

    @PostMapping("/admin/update-overdue")
    public ResponseEntity<ApiResponse<Integer>> updateOverdueBookLoans(){

        return ResponseEntity.ok(ApiResponse.success("Préstamos vencidos actualizados correctamente",
                bookLoanService.updateOverdueBookLoan()));
    }
}
