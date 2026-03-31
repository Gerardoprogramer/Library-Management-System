package com.pm.librarymanagementsystem.controller.users;

import com.pm.librarymanagementsystem.modal.User;
import com.pm.librarymanagementsystem.payload.apiResponse.ApiResponse;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.PageResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.InitiatePaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentResponseDTO;
import com.pm.librarymanagementsystem.payload.dto.response.payment.PaymentStatusResponse;
import com.pm.librarymanagementsystem.service.PaymentService;
import com.pm.librarymanagementsystem.service.UserService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @RequestBody @Valid InitiatePaymentRequest request
    ) {

        User user = userService.getCurrentUserEntity();

        InitiatePaymentResponse response = paymentService.initiatePayment(user.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(
                "Pago iniciado correctamente",
                response
        ));
    }


    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable UUID paymentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                        "Pago obtenido correctamente",
                        paymentService.getPaymentById(paymentId)
                )
        );
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @PathVariable UUID paymentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                        "Estado del pago obtenido correctamente",
                        paymentService.getPaymentStatus(paymentId)
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentHistory(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        return ResponseEntity.ok(ApiResponse.success(
                "Historial de pagos obtenido correctamente",
                paymentService.getPaymentHistory(pageable)
        ));
    }

    @GetMapping("/success-details/{sessionId}")
    public ResponseEntity<PaymentResponseDTO> getDetails(@PathVariable String sessionId) {
        try {
            PaymentResponseDTO details = paymentService.getPaymentDetails(sessionId);
            return ResponseEntity.ok(details);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
