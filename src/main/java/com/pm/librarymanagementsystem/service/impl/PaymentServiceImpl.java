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
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionListLineItemsParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
    public PaymentResponse refundPayment(UUID paymentId) throws BadRequestException {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));

        // Validaciones básicas reales
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Solo pagos exitosos pueden ser reembolsados");
        }

        GatewayRefundResponse refundResponse =
                paymentGatewayService.refundPayment(payment);

        if (!refundResponse.success()) {
            throw new BadRequestException("Error procesando refund en gateway");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);

        if(payment.getPayable() instanceof Subscription s){
            s.setActive(false);
        }


        paymentRepository.save(payment);

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
    public Payment createSubscriptionRenewalPayment(Subscription subscription) {
        Payment payment = new Payment();

        payment.setUser(subscription.getUser());
        payment.setAmount(BigDecimal.valueOf(subscription.getPrice()));
        payment.setCurrency(Currency.USD);
        payment.setPaymentType(PaymentType.MEMBERSHIP);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setRenewalPayment(true);

        payment.setDescription(
                "Auto renewal subscription - " + subscription.getPlanName()
        );

        payment.setPayable(subscription);

        return paymentRepository.save(payment);
    }

    @Override
    public PaymentResponseDTO getPaymentDetails(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);

        Map<String, String> metadata = (session.getMetadata() != null) ? session.getMetadata() : new HashMap<>();
        String typeFromMeta = metadata.getOrDefault("type", "MEMBERSHIP");
        String plan = metadata.getOrDefault("plan", "Plan Estándar");

        PaymentType type;
        try {
            type = PaymentType.valueOf(typeFromMeta);
        } catch (Exception e) {
            type = PaymentType.MEMBERSHIP;
        }

        double amount = 0.0;
        if (session.getAmountTotal() != null) {
            amount = session.getAmountTotal() / 100.0;
        }

        String currency = (session.getCurrency() != null) ? session.getCurrency().toUpperCase() : "USD";
        String status = session.getPaymentStatus();

        String description = "Suscripción a Librería";
        try {
            if (session.getAmountTotal() != null) {
                SessionListLineItemsParams listParams = SessionListLineItemsParams.builder().setLimit(1L).build();
                var lineItems = session.listLineItems(listParams).getData();
                if (!lineItems.isEmpty()) {
                    description = lineItems.get(0).getDescription();
                }
            }
        } catch (Exception e) {
            System.out.println("Waning: No se pudieron recuperar los line items, usando descripción por defecto");
        }

        String customerEmail = "N/A";
        if (session.getCustomerDetails() != null && session.getCustomerDetails().getEmail() != null) {
            customerEmail = session.getCustomerDetails().getEmail();
        }

        return new PaymentResponseDTO(
                amount,
                currency,
                status,
                description,
                customerEmail,
                LocalDateTime.now(),
                type,
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
}
