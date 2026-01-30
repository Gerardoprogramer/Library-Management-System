package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan.SubscriptionPlanResponse;
import com.pm.librarymanagementsystem.payload.dto.request.SubscriptionPlan.CreateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.request.SubscriptionPlan.UpdateSubscriptionPlanRequest;
import org.springframework.data.domain.Pageable;

public interface SubscriptionPlanService {

    SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request);

    SubscriptionPlanResponse updateSubscriptionPlan(long id, UpdateSubscriptionPlanRequest request);

    void deleteSubscriptionPlan(Long id);

    PageResponse<SubscriptionPlanResponse> getAllSubscriptionPlans(Pageable pageable);


}
