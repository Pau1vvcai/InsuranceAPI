package com.example.insuranceapi.order;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String orderNo,
        Long productId,
        String productName,
        String applicantName,
        String applicantPhone,
        String insuredName,
        Relationship relationship,
        BigDecimal premium,
        BigDecimal coverageAmount,
        Integer coverageDays,
        OrderStatus status,
        Instant createdAt
) {

    public static OrderResponse from(InsuranceOrder order) {
        return new OrderResponse(
                order.getOrderNo(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getApplicantName(),
                order.getApplicantPhone(),
                order.getInsuredName(),
                order.getRelationship(),
                order.getPremium(),
                order.getCoverageAmount(),
                order.getCoverageDays(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
