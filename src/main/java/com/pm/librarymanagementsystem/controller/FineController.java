package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.fine.FineRequest;
import com.pm.librarymanagementsystem.payload.dto.request.fine.waiveFineRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.fine.FineResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.service.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    @PostMapping
    public ResponseEntity<ApiResponse<FineResponse>> createFine(
            @Valid @RequestBody FineRequest request
            ){
        return ResponseEntity.ok(ApiResponse.success(
                "Multa creada correctamente",
                fineService.createFine(request)
        ));
    }

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> payFine(
            @PathVariable long fineId, @RequestParam(required = false) String  transactionId
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "se realizo el pago de la multa correctamente",
                fineService.payFine(fineId, transactionId)
        ));
    }

    @PostMapping("/waive")
    public ResponseEntity<ApiResponse<FineResponse>> waiveFine(
            @Valid @RequestBody waiveFineRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Mse eximio la multa correctamente",
                fineService.waiveFine(request)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<FineResponse>>> getMyFines(
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) FineType type,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
            ){
        return ResponseEntity.ok(ApiResponse.success(
                "Listado de multas",
                fineService.getMyFines(status, type, pageable)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FineResponse>>> getAllFines(
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) FineType type,
            @RequestParam(required = false) Long userId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "Listado de multas",
                fineService.getAllFines(status, type, userId, pageable)
        ));
    }
}
