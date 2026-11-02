package com.example.insuranceapi.payment;

import com.example.insuranceapi.common.ResourceNotFoundException;
import com.example.insuranceapi.order.InsuranceOrder;
import com.example.insuranceapi.order.InsuranceOrderRepository;
import com.example.insuranceapi.order.OrderStatus;
import com.example.insuranceapi.policy.Policy;
import com.example.insuranceapi.policy.PolicyRepository;
import com.example.insuranceapi.policy.PolicyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class PaymentService {

    private final InsuranceOrderRepository orderRepository;
    private final PolicyRepository policyRepository;

    public PaymentService(
            InsuranceOrderRepository orderRepository,
            PolicyRepository policyRepository
    ) {
        this.orderRepository = orderRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public PaymentResult pay(String orderNo) {
        InsuranceOrder order = orderRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "保险订单不存在, orderNo=" + orderNo
                ));

        // 重复支付时直接返回已经生成的保单。
        if (order.getStatus() == OrderStatus.PAID) {
            Policy existingPolicy = policyRepository
                    .findByOrderOrderNo(orderNo)
                    .orElseThrow(() -> new IllegalStateException(
                            "订单已支付，但未找到对应保单"
                    ));

            return new PaymentResult(PolicyResponse.from(existingPolicy), false);
        }

        order.markAsPaid();

        LocalDate effectiveDate = LocalDate.now();
        LocalDate expirationDate = effectiveDate.plusDays(
                order.getCoverageDays()
        );

        Policy policy = new Policy(
                generatePolicyNo(),
                order,
                order.getCoverageAmount(),
                effectiveDate,
                expirationDate
        );

        Policy savedPolicy = policyRepository.save(policy);
        return new PaymentResult(PolicyResponse.from(savedPolicy), true);
    }

    private String generatePolicyNo() {
        return "POL-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase();
    }
}
