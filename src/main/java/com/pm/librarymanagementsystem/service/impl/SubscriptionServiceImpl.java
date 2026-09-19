package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.domain.PaymentType;
import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.SubscriptionMapper;

import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionPostResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CancelSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.repository.SubscriptionPlanRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserService userService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public SubscriptionPostResponse subscribe(CreateSubscriptionRequest request) {

        User user = userService.getCurrentUserEntity();

        SubscriptionPlan plan = subscriptionPlanRepository.findById(
                request.subscriptionPlanId()).orElseThrow(
                ()-> new NotFoundException("EL plan no existe"));

        Subscription subscription = SubscriptionMapper.toEntity(request, user, plan);
        subscription.setActive(false);
        subscription = subscriptionRepository.save(subscription);

        InitiatePaymentRequest paymentRequest =
                InitiatePaymentRequest.builder()
                        .payableId(subscription.getId())
                        .paymentType(PaymentType.MEMBERSHIP)
                        .build();

        InitiatePaymentResponse paymentResponse =
                paymentService.initiatePayment(
                        user.getId(),
                        paymentRequest
                );

        SubscriptionResponse response = SubscriptionMapper.toResponse(subscription);

        return new SubscriptionPostResponse(
                response.id(),
                response.planName(),
                response.active(),
                paymentResponse.checkoutUrl()
        );
    }

    @Override
    public SubscriptionResponse getUsersActiveSubscription() {
        return getActiveSubscriptionForUser(
                getCurrentUserId()
        );
    }

    @Override
    public SubscriptionResponse cancelSubscription(UUID id, CancelSubscriptionRequest request) {
        Subscription subscription = subscriptionRepository
                .findById(id)
                .orElseThrow(()-> new NotFoundException("La suscripción no existe"));

        subscription.cancel(request.reason());

        return SubscriptionMapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponse activateSubscription(UUID subscriptionId, UUID paymentId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(()-> new NotFoundException("La suscripción no existe"));

        subscription.setActive(true);

        return SubscriptionMapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Override
    public PageResponse<SubscriptionResponse> getAllSubscriptions(Pageable pageable) {

        Page<SubscriptionResponse> subscriptions = subscriptionRepository.findAll(pageable)
                .map(SubscriptionMapper::toResponse);

        return new PageResponse<>(
                subscriptions.getContent(),
                subscriptions.getNumber(),
                subscriptions.getSize(),
                subscriptions.getTotalElements(),
                subscriptions.getTotalPages(),
                subscriptions.isLast(),
                subscriptions.isFirst(),
                subscriptions.isEmpty()
        );
    }

    @Override
    public void deactivateExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredActiveSubscriptions(LocalDateTime.now());

        for(Subscription subscription: expiredSubscriptions){
            subscription.setActive(false);
            subscriptionRepository.save(subscription);
        }
    }

    @Override
    public SubscriptionResponse getActiveSubscriptionForUser(
            UUID userId
    ) {
        Subscription subscription = subscriptionRepository
                .findActiveSubscriptionByUserId(
                        userId,
                        LocalDateTime.now()
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "No hay una suscripción activa"
                        )
                );

        return SubscriptionMapper.toResponse(subscription);
    }

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
