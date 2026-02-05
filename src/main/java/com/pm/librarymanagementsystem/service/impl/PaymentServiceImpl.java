package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.Currency;
import com.pm.librarymanagementsystem.domain.PaymentGateway;
import com.pm.librarymanagementsystem.domain.PaymentStatus;
import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.PaymentMapper;
import com.pm.librarymanagementsystem.mapper.UserMapper;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.*;
import com.pm.librarymanagementsystem.repository.PaymentRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.repository.UserRepository;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;

    @Override
    public InitiatePaymentResponse initiatePayment(Long userId, InitiatePaymentRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Mapper base
        Payment payment = PaymentMapper.fromInitiateRequest(request);

        payment.setUser(user);
        payment.setSubscription(subscription);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setInitiatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        // Stripe Session
        GatewayPaymentResponse gatewayResponse =
                paymentGatewayService.createCheckoutSession(payment, request);

        payment.setCheckoutSessionId(gatewayResponse.checkoutSessionId());
        payment.setPaymentIntentId(gatewayResponse.paymentIntentId());

        payment = paymentRepository.save(payment);

        return new InitiatePaymentResponse(
                payment.getId(),
                payment.getPaymentStatus(),
                gatewayResponse.checkoutUrl(),
                gatewayResponse.checkoutSessionId()
        );
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return PaymentMapper.toResponse(payment);
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return PaymentMapper.toStatusResponse(payment);
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId) throws BadRequestException {

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
        payment.getSubscription().setActive(false);


        paymentRepository.save(payment);

        return PaymentMapper.toResponse(payment);
    }

    @Override
    public PageResponse<PaymentResponse> getPaymentHistory(Pageable pageable) {
        User user  = userService.getCurrentUserEntity();

        Page<Payment> payment = paymentRepository.findByUserId(user.getId(), pageable);
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

        payment.setSubscription(subscription);

        return paymentRepository.save(payment);
    }

}
