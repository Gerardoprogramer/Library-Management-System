package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.FineStatus;
import com.pm.librarymanagementsystem.domain.FineType;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.FineMapper;
import com.pm.librarymanagementsystem.modal.BookLoan;
import com.pm.librarymanagementsystem.modal.Fine;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.fine.FineRequest;
import com.pm.librarymanagementsystem.payload.dto.request.fine.waiveFineRequest;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.fine.FineResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.repository.BookLoanRepository;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.service.FineService;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final BookLoanRepository bookLoanRepository;
    private final FineRepository fineRepository;
    private final UserService userService;
    private final PaymentService paymentService;

    @Override
    public FineResponse createFine(FineRequest request) {
        BookLoan bookLoan = bookLoanRepository.findById(request.bookLoanId())
                .orElseThrow(()-> new NotFoundException("Préstamo de libro no encontrado"));

        Fine fine = Fine.builder()
                .bookLoan(bookLoan)
                .user(bookLoan.getUser())
                .type(request.type())
                .currency(request.currency())
                .amount(request.amount())
                .status(FineStatus.PENDING)
                .reason(request.reason())
                .notes(request.notes())
                .build();

        return FineMapper.toResponse(fineRepository.save(fine));
    }

    @Override
    public InitiatePaymentResponse payFine(Long fineId, String transactionId) {

        Fine fine = fineRepository.findById(fineId).orElseThrow(
                ()-> new NotFoundException("Multa no encontrada"));

        if(fine.getStatus().equals(FineStatus.PAID)){
            throw new BusinessRuleException("Multa ya pagada");
        }

        if(fine.getStatus().equals(FineStatus.WAIVED)){
            throw new BusinessRuleException("Multa eximida");
        }

        User user = userService.getCurrentUserEntity();

        InitiatePaymentRequest request = InitiatePaymentRequest
                .builder()
                .payableId(fine.getId())
                .paymentType(PaymentType.FINE)
                .amount(fine.getAmount())
                .currency(fine.getCurrency())
                .description("pago de multas de la biblioteca")
                .successUrl("http://localhost:5173/success")
                .cancelUrl("http://localhost:5173/cancel")
                .build();

        return paymentService.initiatePayment(user.getId(), request);
    }

    @Override
    public void markFineAsPaid(Long fineId, BigDecimal amount, String transactionId) {

        Fine fine = fineRepository.findById(fineId).orElseThrow(
                ()-> new NotFoundException("Multa no encontrada"));

        fine.applyPayment(amount);
        fine.setTransactionId(transactionId);
        fine.setStatus(FineStatus.PAID);
        fine.setUpdatedAt(LocalDateTime.now());

        fineRepository.save(fine);
    }

    @Override
    public FineResponse waiveFine(waiveFineRequest request) {
        Fine fine = fineRepository.findById(request.fineId())
                .orElseThrow( ()-> new NotFoundException("Multa no encontrada"));

        if(fine.getStatus() == FineStatus.WAIVED){
            throw new BusinessRuleException("La multa ya ha sido pagada.");
        }

        if(fine.getStatus() == FineStatus.PAID){
            throw new BusinessRuleException("La multa ya se ha pagado y no se puede eximir.");
        }

        User currentAdmin = userService.getCurrentUserEntity();
        fine.waive(currentAdmin, request.reason());


        return FineMapper.toResponse(fineRepository.save(fine));
    }

    @Override
    public PageResponse<FineResponse> getMyFines(FineStatus status, FineType type, Pageable pageable) {
        User user = userService.getCurrentUserEntity();

        Page<Fine> fines = fineRepository
                .findAllWithFilters(user.getId(), status, type, pageable);
        Page<FineResponse> mappedPage = fines.map(FineMapper::toResponse);

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
    public PageResponse<FineResponse> getAllFines(
            FineStatus status, FineType type, Long userId, Pageable pageable) {

        Page<Fine> fines = fineRepository
                .findAllWithFilters(userId, status, type, pageable);
        Page<FineResponse> mappedPage = fines.map(FineMapper::toResponse);

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
}
