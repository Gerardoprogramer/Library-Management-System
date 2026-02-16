package com.pm.librarymanagementsystem.controller.admin;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.SubscriptionPlan.CreateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.request.SubscriptionPlan.UpdateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan.SubscriptionPlanResponse;
import com.pm.librarymanagementsystem.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscription-plans")
public class AdminSubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createSubscriptionPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Se creó el plan correctamente",
                        subscriptionPlanService.createSubscriptionPlan(request)
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updateSubscriptionPlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request){

        return ResponseEntity.ok(ApiResponse.success(
                "El plan se actualizó correctamente",
                subscriptionPlanService.updateSubscriptionPlan(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPlan(
            @PathVariable UUID id){

        subscriptionPlanService.deleteSubscriptionPlan(id);
        return ResponseEntity.ok(ApiResponse.success("Se eliminó correctamente"));
    }
}
