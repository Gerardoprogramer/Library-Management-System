package com.pm.librarymanagementsystem.service;

import com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan.SubscriptionPlanResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.CreateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.UpdateSubscriptionPlanRequest;
import org.springframework.data.domain.Page;

public interface SubscriptionPlanService {

    SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request);

    SubscriptionPlanResponse updateSubscriptionPlan(long id, UpdateSubscriptionPlanRequest request);

    void deleteSubscriptionPlan(Long id);

    Page<SubscriptionPlanResponse> getAllSubscriptionPlans(int page, int size);


}
