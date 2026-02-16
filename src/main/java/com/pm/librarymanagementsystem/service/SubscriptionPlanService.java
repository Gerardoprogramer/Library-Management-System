package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan.SubscriptionPlanResponse;
import com.pm.librarymanagementsystem.payload.dto.request.SubscriptionPlan.CreateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.request.SubscriptionPlan.UpdateSubscriptionPlanRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SubscriptionPlanService {

    SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request);

    SubscriptionPlanResponse updateSubscriptionPlan(UUID id, UpdateSubscriptionPlanRequest request);

    void deleteSubscriptionPlan(UUID id);

    PageResponse<SubscriptionPlanResponse> getAllSubscriptionPlans(Pageable pageable);


}
