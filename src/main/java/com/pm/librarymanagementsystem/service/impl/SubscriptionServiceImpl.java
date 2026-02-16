package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.exception.NotFoundException;
import com.pm.librarymanagementsystem.mapper.SubscriptionMapper;

import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.modal.SubscriptionPlan;
import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CancelSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;
import com.pm.librarymanagementsystem.repository.SubscriptionPlanRepository;
import com.pm.librarymanagementsystem.repository.SubscriptionRepository;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import com.pm.librarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserService userService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public SubscriptionResponse subscribe(CreateSubscriptionRequest request) {

        User user = userService.getCurrentUserEntity();

        SubscriptionPlan plan = subscriptionPlanRepository.findById(
                request.subscriptionPlanId()).orElseThrow(
                ()-> new NotFoundException("EL plan no existe"));

        Subscription subscription = SubscriptionMapper.toEntity(request, user, plan);
        //se activa cuando tenga el payment
        subscription.setActive(false);

        return SubscriptionMapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Override
    public SubscriptionResponse getUsersActiveSubscription() {

        User user = userService.getCurrentUserEntity();
        Subscription subscription = subscriptionRepository
                .findActiveSubscriptionByUserId(user.getId(), LocalDateTime.now())
                .orElseThrow(()-> new NotFoundException("No hay una suscripción activa"));

        return SubscriptionMapper.toResponse(subscription);
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
}
