package com.pm.librarymanagementsystem.service.gateway;

import com.pm.librarymanagementsystem.configurations.StripeConfig;
import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.modal.Subscription;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StripePaymentGatewayService implements PaymentGatewayService {

    private final StripeConfig stripeConfig;

    @Override
    public GatewayPaymentResponse createCheckoutSession(
            Payment payment
    ) {
        try {

            long amountInMinorUnits = payment.getAmount()
                    .movePointRight(2)
                    .longValueExact();

            SessionCreateParams.PaymentIntentData paymentIntentData =
                    SessionCreateParams.PaymentIntentData.builder()
                            .putMetadata(
                                    "paymentId",
                                    payment.getId().toString()
                            )
                            .build();

            SessionCreateParams.Builder builder =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setPaymentIntentData(paymentIntentData)
                            .setSuccessUrl(
                                    stripeConfig.getSuccessUrl()
                            )
                            .setCancelUrl(
                                    stripeConfig.getCancelUrl()
                            )
                            .addPaymentMethodType(
                                    SessionCreateParams.PaymentMethodType.CARD
                            )
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency(
                                                                    payment.getCurrency()
                                                                            .name()
                                                                            .toLowerCase()
                                                            )
                                                            .setUnitAmount(amountInMinorUnits)
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName(payment.getDescription())
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .putMetadata(
                                    "paymentId",
                                    payment.getId().toString()
                            )
                            .putMetadata(
                                    "type",
                                    payment.getPaymentType()
                                            .name()
                                            .toLowerCase()
                            );

            if (payment.getPayable() instanceof Subscription subscription) {
                builder.putMetadata(
                        "plan",
                        subscription.getPlanName()
                );
            }

            RequestOptions requestOptions =
                    RequestOptions.builder()
                            .setIdempotencyKey(
                                    "checkout-payment-"
                                            + payment.getId()
                            )
                            .build();

            Session session =
                    Session.create(
                            builder.build(),
                            requestOptions
                    );

            return new GatewayPaymentResponse(
                    session.getUrl(),
                    session.getId(),
                    session.getPaymentIntent()
            );

        } catch (StripeException e) {
            throw new RuntimeException(
                    "Error creando sesión de pago en Stripe",
                    e
            );
        }
    }

    @Override
    public GatewayRefundResponse refundPayment(Payment payment) {
        try {
            RefundCreateParams params =
                    RefundCreateParams.builder()
                            .setPaymentIntent(
                                    payment.getPaymentIntentId()
                            )
                            .build();

            RequestOptions requestOptions =
                    RequestOptions.builder()
                            .setIdempotencyKey(
                                    "refund-payment-" + payment.getId()
                            )
                            .build();

            Refund refund = Refund.create(
                    params,
                    requestOptions
            );

            return new GatewayRefundResponse(
                    true,
                    refund.getId(),
                    refund.getStatus()
            );

        } catch (StripeException exception) {
            return new GatewayRefundResponse(
                    false,
                    null,
                    exception.getMessage()
            );
        }
    }
}

