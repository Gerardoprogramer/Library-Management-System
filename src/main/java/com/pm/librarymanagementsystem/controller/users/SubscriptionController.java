package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CancelSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.request.Subscription.CreateSubscriptionRequest;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody CreateSubscriptionRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Se realizó la suscripción correctamente",
                        subscriptionService.subscribe(request)
                ));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getUsersActiveSubscription(){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Se encontró la suscripción activa",
                        subscriptionService.getUsersActiveSubscription()
                ));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody CancelSubscriptionRequest request){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Se canceló correctamente",
                        subscriptionService.cancelSubscription(id, request)
                ));
    }

    @PatchMapping("/activate")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> activateSubscription(
            @RequestParam UUID subscriptionId,
            @RequestParam UUID paymentId){
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Se activó correctamente",
                        subscriptionService.activateSubscription(subscriptionId, paymentId)
                ));
    }
}
