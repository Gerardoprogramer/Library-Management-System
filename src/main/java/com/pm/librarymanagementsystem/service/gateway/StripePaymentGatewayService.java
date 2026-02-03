package com.pm.librarymanagementsystem.service.gateway;

import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripePaymentGatewayService implements PaymentGatewayService {

    @Override
    public GatewayPaymentResponse createCheckoutSession(
            Payment payment,
            InitiatePaymentRequest request
    ) {

        try {

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)

                            .setSuccessUrl(request.successUrl())
                            .setCancelUrl(request.cancelUrl())

                            .addPaymentMethodType(
                                    SessionCreateParams.PaymentMethodType.CARD
                            )

                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency(request.currency().name().toLowerCase())
                                                            .setUnitAmount(
                                                                    request.amount()
                                                                            .multiply(BigDecimal.valueOf(100))
                                                                            .longValue()
                                                            )
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName(request.description())
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )

                            .putMetadata("paymentId", payment.getId().toString())

                            .build();

            Session session = Session.create(params);

            return new GatewayPaymentResponse(
                    session.getUrl(),
                    session.getId(),
                    session.getPaymentIntent()
            );

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error", e);
        }
    }

    @Override
    public GatewayRefundResponse refundPayment(Payment payment) {
        // Aquí después iría Stripe SDK refund.create()

        return new GatewayRefundResponse(
                true,
                "re_fake_123",
                "Refund procesado correctamente"
        );
    }
}

