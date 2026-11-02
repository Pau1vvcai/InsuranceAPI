//保险产品数据库模型
package com.example.insuranceapi.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "insurance_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premium;

    @Column(nullable = false)
    private Integer coverageDays;

    public InsuranceProduct(
            String code,
            String name,
            String description,
            BigDecimal coverageAmount,
            BigDecimal premium,
            Integer coverageDays
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.coverageAmount = coverageAmount;
        this.premium = premium;
        this.coverageDays = coverageDays;
    }
}