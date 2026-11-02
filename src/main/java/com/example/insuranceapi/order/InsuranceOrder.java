//创建订单实体
package com.example.insuranceapi.order;

import com.example.insuranceapi.product.InsuranceProduct;
import com.example.insuranceapi.common.InvalidOrderStateException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(name = "insurance_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsuranceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(nullable = false, length = 50)
    private String applicantName;

    @Column(nullable = false, length = 18)
    private String applicantIdCard;

    @Column(nullable = false, length = 20)
    private String applicantPhone;

    @Column(nullable = false, length = 50)
    private String insuredName;

    @Column(nullable = false, length = 18)
    private String insuredIdCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Relationship relationship;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premium;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false)
    private Integer coverageDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public InsuranceOrder(
            String orderNo,
            InsuranceProduct product,
            String applicantName,
            String applicantIdCard,
            String applicantPhone,
            String insuredName,
            String insuredIdCard,
            Relationship relationship
    ) {
        this.orderNo = orderNo;
        this.product = product;
        this.applicantName = applicantName;
        this.applicantIdCard = applicantIdCard;
        this.applicantPhone = applicantPhone;
        this.insuredName = insuredName;
        this.insuredIdCard = insuredIdCard;
        this.relationship = relationship;

        // 下单时保存保费快照，避免产品以后调价影响历史订单。
        this.premium = product.getPremium();
        this.coverageAmount = product.getCoverageAmount();
        this.coverageDays = product.getCoverageDays();
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = Instant.now();
    }

    public void markAsPaid() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateException("只有待支付订单才能支付");
        }

        this.status = OrderStatus.PAID;
    }
}
