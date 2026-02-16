package com.pm.librarymanagementsystem.controller.admin;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.bookLoan.BookLoanCheckoutRequest;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/book-loans")
public class AdminBookLoanController {

    private final BookLoanService bookLoanService;

    @PostMapping("/users/{userId}/checkout")
    public ResponseEntity<ApiResponse<BookLoanResponse>> checkoutForUser(
            @PathVariable UUID userId,
            @Valid @RequestBody BookLoanCheckoutRequest request
    ){

        return ResponseEntity.ok(ApiResponse.success(
                "Préstamo creado correctamente",
                bookLoanService.checkoutBookForUser(userId, request)
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

    @PutMapping("/overdue/update")
    public ResponseEntity<ApiResponse<Integer>> updateOverdueBookLoans(){

        return ResponseEntity.ok(ApiResponse.success("Préstamos vencidos actualizados correctamente",
                bookLoanService.updateOverdueBookLoan()));
    }
}
