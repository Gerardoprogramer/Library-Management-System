package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CancelSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {

    SubscriptionResponse subscribe(CreateSubscriptionRequest request);

    SubscriptionResponse getUsersActiveSubscription();

    SubscriptionResponse cancelSubscription(Long id, CancelSubscriptionRequest request);

    SubscriptionResponse activateSubscription(Long subscriptionId, Long paymentId);

    PageResponse<SubscriptionResponse> getAllSubscriptions(Pageable pageable);

    void deactivateExpiredSubscriptions();
}
