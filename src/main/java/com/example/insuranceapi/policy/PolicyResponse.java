package com.example.insuranceapi.policy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PolicyResponse(
        String policyNo,
        String orderNo,
        String productName,
        String insuredName,
        BigDecimal coverageAmount,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        Instant createdAt
) {

    public static PolicyResponse from(Policy policy) {
        return new PolicyResponse(
                policy.getPolicyNo(),
                policy.getOrder().getOrderNo(),
                policy.getOrder().getProduct().getName(),
                policy.getOrder().getInsuredName(),
                policy.getCoverageAmount(),
                policy.getEffectiveDate(),
                policy.getExpirationDate(),
                policy.getCreatedAt()
        );
    }
}