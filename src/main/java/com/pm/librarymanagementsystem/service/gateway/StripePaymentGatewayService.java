package com.pm.librarymanagementsystem.service.gateway;

import com.pm.librarymanagementsystem.modal.Payment;
import com.pm.librarymanagementsystem.payload.dto.request.payment.InitiatePaymentRequest;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayPaymentResponse;
import com.pm.librarymanagementsystem.payload.dto.response.payment.GatewayRefundResponse;
import com.pm.librarymanagementsystem.service.PaymentGatewayService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
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

                            .setSuccessUrl("https://obsidian-delta-kohl.vercel.app/payment/success?session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl("https://obsidian-delta-kohl.vercel.app/dashboard/subscription")

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
                            .putMetadata("type", request.paymentType().toString().toLowerCase())
                            .putMetadata("plan", request.plan())

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
        try {

            RefundCreateParams params =
                    RefundCreateParams.builder()
                            .setPaymentIntent(payment.getPaymentIntentId())
                            .build();

            Refund refund = Refund.create(params);

            return new GatewayRefundResponse(
                    true,
                    refund.getId(),
                    refund.getStatus()
            );

        } catch (StripeException e) {

            return new GatewayRefundResponse(
                    false,
                    null,
                    e.getMessage()
            );
        }
    }

    @Override
    public GatewayPaymentResponse createCheckoutSessionForRenewal(Payment payment) {

        try {

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)

                            .setSuccessUrl("https://obsidian-delta-kohl.vercel.app/payment/success?session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl("https://obsidian-delta-kohl.vercel.app/dashboard/subscription")

                            .addPaymentMethodType(
                                    SessionCreateParams.PaymentMethodType.CARD
                            )

                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency("usd")
                                                            .setUnitAmount(
                                                                    payment.getAmount()
                                                                            .multiply(BigDecimal.valueOf(100))
                                                                            .longValue()
                                                            )
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName(payment.getDescription())
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
            throw new RuntimeException("Stripe renewal error", e);
        }
    }
}

