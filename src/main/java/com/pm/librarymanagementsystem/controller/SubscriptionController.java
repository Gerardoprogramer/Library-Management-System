package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CancelSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody CreateSubscriptionRequest request){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "se realizó la suscripción correctamente",
                        subscriptionService.subscribe(request))
        );
    }

    @GetMapping("/user/active")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getUsersActiveSubscription(){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "se encontró la suscripción activa",
                        subscriptionService.getUsersActiveSubscription()
                ));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionResponse>>> getAllSubscriptions (
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listado de suscripciones",
                        subscriptionService.getAllSubscriptions(pageable))
        );
    }

    @PostMapping("/admin/deactivate-expired")
    public ResponseEntity<ApiResponse<Void>> deactivateExpiredSubscriptions (){

        subscriptionService.deactivateExpiredSubscriptions();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tarea completada"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @PathVariable Long id, @Valid @RequestBody CancelSubscriptionRequest request
            ){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "se cancelo correctamente",
                        subscriptionService.cancelSubscription(id, request)
                ));
    }

    @PatchMapping("/activate")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> activateSubscription(
            @RequestParam Long subscriptionId, @RequestParam Long paymentId
    ){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "se activó correctamente",
                        subscriptionService.activateSubscription(subscriptionId, paymentId)
                ));
    }
}
