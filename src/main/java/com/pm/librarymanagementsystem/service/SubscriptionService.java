package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionPostResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CancelSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SubscriptionService {

    SubscriptionPostResponse subscribe(CreateSubscriptionRequest request);

    SubscriptionResponse getUsersActiveSubscription();

    SubscriptionResponse cancelSubscription(UUID id, CancelSubscriptionRequest request);

    SubscriptionResponse activateSubscription(UUID subscriptionId, UUID paymentId);

    PageResponse<SubscriptionResponse> getAllSubscriptions(Pageable pageable);

    void deactivateExpiredSubscriptions();
}
