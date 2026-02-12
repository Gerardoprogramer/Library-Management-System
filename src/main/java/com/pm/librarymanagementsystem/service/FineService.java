package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;
import com.pm.librarymanagementsystem.payload.dto.request.fine.FineRequest;
import com.pm.librarymanagementsystem.payload.dto.request.fine.waiveFineRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.fine.FineResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface FineService {

    FineResponse createFine(FineRequest request);

    InitiatePaymentResponse payFine(Long fineId, String transactionId);

    void markFineAsPaid(Long fineId, BigDecimal amount, String transactionId);

    FineResponse waiveFine(waiveFineRequest request);

    PageResponse<FineResponse> getMyFines(FineStatus status, FineType type, Pageable pageable);

    PageResponse<FineResponse> getAllFines(FineStatus status, FineType type, Long userId, Pageable pageable);
}
