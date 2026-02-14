package com.pm.librarymanagementsystem.controller.admin;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.Subscription.SubscriptionResponse;
import com.pm.librarymanagementsystem.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subscriptions")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionResponse>>> getAllSubscriptions(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable){

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listado de suscripciones",
                        subscriptionService.getAllSubscriptions(pageable)
                ));
    }

    @PostMapping("/deactivate-expired")
    public ResponseEntity<ApiResponse<Void>> deactivateExpiredSubscriptions(){
        subscriptionService.deactivateExpiredSubscriptions();
        return ResponseEntity.ok(ApiResponse.success("Tarea completada"));
    }
}
