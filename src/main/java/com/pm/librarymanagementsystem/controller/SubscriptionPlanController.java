package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.SubscriptionPlan.SubscriptionPlanResponse;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.CreateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.payload.dto.resquest.SubscriptionPlan.UpdateSubscriptionPlanRequest;
import com.pm.librarymanagementsystem.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription-plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createSubscriptionPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request){

        SubscriptionPlanResponse subscriptionPlanResponse = subscriptionPlanService.createSubscriptionPlan(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                "Se creo el plan correctamente",
                subscriptionPlanResponse
                ));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updateSubscriptionPlan(
            @PathVariable Long id, @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        SubscriptionPlanResponse subscriptionPlanResponse =
                subscriptionPlanService.updateSubscriptionPlan(id, request);

        return ResponseEntity.ok(ApiResponse.success(
                "El plan se actualizo correctamente",
                subscriptionPlanResponse
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SubscriptionPlanResponse>>> getAllSubscriptionPlans(
            @RequestParam(defaultValue = "0") int page,
            @Max(50) @Min(1) @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(ApiResponse.success("Listado de planes",
                subscriptionPlanService.getAllSubscriptionPlans(page, size)));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPlan(@PathVariable Long id){
        subscriptionPlanService.deleteSubscriptionPlan(id);

        return ResponseEntity.ok(ApiResponse.success("Se elimino correctamente"));
    }
}
