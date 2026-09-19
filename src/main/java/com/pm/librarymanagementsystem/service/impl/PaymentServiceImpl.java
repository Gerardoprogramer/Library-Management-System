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
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.pm.librarymanagementsystem.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
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
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FineRepository fineRepository;

    @Override
    public InitiatePaymentResponse initiatePayment(
            UUID userId,
            InitiatePaymentRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Usuario no encontrado")
                );

        Payable payable = resolvePayable(
                userId,
                request
        );

        Payment payment = createPayment(
                user,
                payable,
                request.paymentType()
        );

        payment = paymentRepository.save(payment);

        GatewayPaymentResponse gatewayResponse =
                paymentGatewayService.createCheckoutSession(payment);

        payment.setCheckoutSessionId(
                gatewayResponse.checkoutSessionId()
        );

        payment.setPaymentIntentId(
                gatewayResponse.paymentIntentId()
        );

        return new InitiatePaymentResponse(
                payment.getId(),
                payment.getPaymentStatus(),
                gatewayResponse.checkoutUrl(),
                gatewayResponse.checkoutSessionId()
        );
    }

    @Override
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
    public PaymentResponse refundPayment(UUID paymentId) {

        Payment payment = paymentRepository
                .findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Pago no encontrado"
                        )
                );

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessRuleException(
                    "El pago ya fue reembolsado"
            );
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessRuleException(
                    "Solo los pagos exitosos pueden ser reembolsados"
            );
        }

        if (payment.getPaymentIntentId() == null
                || payment.getPaymentIntentId().isBlank()) {

            throw new BusinessRuleException(
                    "El pago no tiene una transacción válida para reembolso"
            );
        }

        GatewayRefundResponse refundResponse =
                paymentGatewayService.refundPayment(payment);

        if (!refundResponse.success()) {
            throw new BusinessRuleException(
                    "No se pudo procesar el reembolso"
            );
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundId(refundResponse.refundId());
        payment.setRefundedAt(LocalDateTime.now());

        rollbackPayableAfterRefund(payment);

        return PaymentMapper.toResponse(payment);
    }

    @Override
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


    private Payable resolvePayable(
            UUID userId,
            InitiatePaymentRequest request
    ) {
        return switch (request.paymentType()) {

            case MEMBERSHIP -> {
                Subscription subscription =
                        subscriptionRepository
                                .findById(request.payableId())
                                .orElseThrow(() ->
                                        new NotFoundException(
                                                "Suscripción no encontrada"
                                        )
                                );

                validateOwnership(subscription, userId);

                if (subscription.isCurrentlyActive()) {
                    throw new BusinessRuleException(
                            "La suscripción ya se encuentra activa"
                    );
                }

                yield subscription;
            }

            case FINE -> {
                Fine fine = fineRepository
                        .findById(request.payableId())
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Multa no encontrada"
                                )
                        );

                validateOwnership(fine, userId);

                if (fine.getStatus() != FineStatus.PENDING) {
                    throw new BusinessRuleException(
                            "La multa no está disponible para pago"
                    );
                }

                yield fine;
            }

            default -> throw new BusinessRuleException(
                    "Tipo de pago no soportado"
            );
        };
    }

    private void validateOwnership(
            Payable payable,
            UUID userId
    ) {
        if (payable.getUser() == null ||
                !payable.getUser().getId().equals(userId)) {

            throw new NotFoundException(
                    "Recurso de pago no encontrado"
            );
        }
    }

    private Payment createPayment(
            User user,
            Payable payable,
            PaymentType paymentType
    ) {
        Payment payment = new Payment();

        payment.setUser(user);
        payment.setPayable(payable);
        payment.setPaymentType(paymentType);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setInitiatedAt(LocalDateTime.now());

        if (payable instanceof Subscription subscription) {

            payment.setAmount(
                    BigDecimal.valueOf(
                            subscription.getPrice(),
                            2
                    )
            );

            payment.setCurrency(Currency.USD);

            payment.setDescription(
                    "Suscripción al plan: "
                            + subscription.getPlanName()
            );

        } else if (payable instanceof Fine fine) {

            payment.setAmount(fine.getAmount());
            payment.setCurrency(fine.getCurrency());
            payment.setDescription("Pago de multa");
        }

        return payment;
    }

    private void rollbackPayableAfterRefund(
            Payment payment
    ) {
        Payable payable =
                (Payable) Hibernate.unproxy(
                        payment.getPayable()
                );

        if (payable instanceof Fine fine) {

            fine.reopenAfterRefund();
            fineRepository.save(fine);

        } else if (payable instanceof Subscription subscription) {

            subscription.cancel(
                    "Suscripción cancelada por reembolso"
            );

            subscription.setAutoRenew(false);

            subscriptionRepository.save(subscription);
        }
    }
}
