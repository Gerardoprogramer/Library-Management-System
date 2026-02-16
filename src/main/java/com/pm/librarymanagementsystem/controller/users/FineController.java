package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.fine.FineResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fines")
public class FineController {

    private final FineService fineService;

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> payFine(
            @PathVariable UUID fineId, @RequestParam(required = false) String  transactionId
    ){
        return ResponseEntity.ok(ApiResponse.success(
                "se realizo el pago de la multa correctamente",
                fineService.payFine(fineId, transactionId)
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
}
