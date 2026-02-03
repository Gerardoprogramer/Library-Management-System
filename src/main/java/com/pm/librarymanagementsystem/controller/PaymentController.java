package com.pm.librarymanagementsystem.controller;

import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentStatusResponse;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/initiate")
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(
            @RequestBody @Valid InitiatePaymentRequest request
    ) {

        User user = userService.getCurrentUserEntity();

        InitiatePaymentResponse response = paymentService.initiatePayment(user.getId(), request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId)
        );
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentStatus(paymentId)
        );
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long paymentId
    ) throws Exception {

        return ResponseEntity.ok(
                paymentService.refundPayment(paymentId)
        );
    }
}
