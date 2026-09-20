package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.*;
import com.pm.librarymanagementsystem.exception.BusinessRuleException;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.PaymentMapper;
import com.pm.librarymanagementsystem.modal.*;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.*;
import com.pm.librarymanagementsystem.repository.FineRepository;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.pm.librarymanagementsystem.service.PaymentService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final SubscriptionRepository subscriptionRepository;
    private final FineRepository fineRepository;
    private final PaymentInitiationProcessor paymentInitiationProcessor;
    private final PaymentRefundProcessor paymentRefundProcessor;

    @Override
    public InitiatePaymentResponse initiatePayment(
            UUID userId,
            InitiatePaymentRequest request
    ) {

        Payment payment =
                paymentInitiationProcessor
                        .preparePayment(
                                userId,
                                request
                        );

        GatewayPaymentResponse gatewayResponse =
                paymentGatewayService
                        .createCheckoutSession(
                                payment
                        );

        paymentInitiationProcessor
                .storeGatewayReferences(
                        payment.getId(),
                        gatewayResponse
                );

        return new InitiatePaymentResponse(
                payment.getId(),
                payment.getPaymentStatus(),
                gatewayResponse.checkoutUrl(),
                gatewayResponse.checkoutSessionId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {

        Payment payment = paymentRepository
                .findByIdAndUser_Id(
                        paymentId,
                        getCurrentUserId()
                )
                .orElseThrow(() ->
                        new NotFoundException("Pago no encontrado")
                );

        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(
            UUID paymentId
    ) {
        Payment payment = paymentRepository
                .findByIdAndUser_Id(
                        paymentId,
                        getCurrentUserId()
                )
                .orElseThrow(() ->
                        new NotFoundException("Pago no encontrado")
                );

        return PaymentMapper.toStatusResponse(payment);
    }

    @Override
    public PaymentResponse refundPayment(
            UUID paymentId
    ) {

        Payment payment =
                paymentRefundProcessor
                        .prepareRefund(
                                paymentId
                        );

        GatewayRefundResponse refundResponse =
                paymentGatewayService
                        .refundPayment(
                                payment
                        );

        if (!refundResponse.success()) {
            throw new BusinessRuleException(
                    "No se pudo procesar el reembolso"
            );
        }

        Payment refundedPayment =
                paymentRefundProcessor
                        .applyRefund(
                                paymentId,
                                refundResponse
                        );

        return PaymentMapper.toResponse(
                refundedPayment
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPaymentHistory(Pageable pageable) {

        Page<Payment> payment = paymentRepository.findByUserId(getCurrentUserId(), pageable);
        Page<PaymentResponse> mappedPage = payment.map(PaymentMapper::toResponse);

        return new PageResponse<>(
                mappedPage.getContent(),
                mappedPage.getNumber(),
                mappedPage.getSize(),
                mappedPage.getTotalElements(),
                mappedPage.getTotalPages(),
                mappedPage.isLast(),
                mappedPage.isFirst(),
                mappedPage.isEmpty());
    }

    @Override
    @Transactional
    public Payment createSubscriptionRenewalPayment(
            Subscription subscription
    ) {
        Payment payment = new Payment();

        payment.setUser(subscription.getUser());

        payment.setAmount(
                BigDecimal.valueOf(
                        subscription.getPrice(),
                        2
                )
        );

        payment.setCurrency(Currency.USD);
        payment.setPaymentType(PaymentType.MEMBERSHIP);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setRenewalPayment(true);
        payment.setInitiatedAt(LocalDateTime.now());

        payment.setDescription(
                "Renovación de suscripción: "
                        + subscription.getPlanName()
        );

        payment.setPayable(subscription);

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentDetails(
            String sessionId
    ) {
        Payment payment = paymentRepository
                .findByCheckoutSessionIdAndUser_Id(
                        sessionId,
                        getCurrentUserId()
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "Pago no encontrado"
                        )
                );

        String plan = null;

        if (payment.getPayable() instanceof Subscription subscription) {
            plan = subscription.getPlanName();
        }

        LocalDateTime date =
                payment.getCompletedAt() != null
                        ? payment.getCompletedAt()
                        : payment.getCreatedAt();

        return new PaymentResponseDTO(
                payment.getAmount().doubleValue(),
                payment.getCurrency().name(),
                payment.getPaymentStatus().name(),
                payment.getDescription(),
                payment.getUser().getEmail(),
                date,
                payment.getPaymentType(),
                plan
        );
    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
