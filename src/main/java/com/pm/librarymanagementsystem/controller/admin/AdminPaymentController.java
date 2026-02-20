package com.pm.librarymanagementsystem.controller.admin;

import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponse;
import com.pm.librarymanagementsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable UUID paymentId
    ) throws Exception {

        return ResponseEntity.ok(ApiResponse.success(
                        "Reembolso procesado correctamente",
                        paymentService.refundPayment(paymentId)
                )
        );
    }
}
