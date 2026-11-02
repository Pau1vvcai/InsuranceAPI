package com.example.insuranceapi.payment;

import com.example.insuranceapi.policy.PolicyResponse;

public record PaymentResult(
        PolicyResponse policy,
        boolean created
) {
}
