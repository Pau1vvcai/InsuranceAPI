package com.example.insuranceapi.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal coverageAmount,
        BigDecimal premium,
        Integer coverageDays
) {

    public static ProductResponse from(InsuranceProduct product) {
        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getCoverageAmount(),
                product.getPremium(),
                product.getCoverageDays()
        );
    }
}