package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;
import com.pm.librarymanagementsystem.payload.dto.request.fine.FineRequest;
import com.pm.librarymanagementsystem.payload.dto.request.fine.WaiveFineRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.fine.FineResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface FineService {

    FineResponse createFine(FineRequest request);

    InitiatePaymentResponse payFine(UUID fineId, String transactionId);

    void markFineAsPaid(UUID fineId, BigDecimal amount, String transactionId);

    FineResponse waiveFine(WaiveFineRequest request);

    PageResponse<FineResponse> getMyFines(FineStatus status, FineType type, Pageable pageable);

    PageResponse<FineResponse> getAllFines(FineStatus status, FineType type, UUID userId, Pageable pageable);
}
