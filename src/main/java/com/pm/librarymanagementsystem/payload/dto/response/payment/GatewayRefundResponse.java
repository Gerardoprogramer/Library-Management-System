package com.pm.librarymanagementsystem.payload.dto.response.payment;

public record GatewayRefundResponse(
        boolean success,
        String refundId,
        String message
) {}