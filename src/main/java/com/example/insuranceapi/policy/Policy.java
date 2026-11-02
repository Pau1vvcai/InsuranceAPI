package com.example.insuranceapi.policy;

import com.example.insuranceapi.order.InsuranceOrder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "policies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String policyNo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private InsuranceOrder order;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Policy(
            String policyNo,
            InsuranceOrder order,
            BigDecimal coverageAmount,
            LocalDate effectiveDate,
            LocalDate expirationDate
    ) {
        this.policyNo = policyNo;
        this.order = order;
        this.coverageAmount = coverageAmount;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.createdAt = Instant.now();
    }
}