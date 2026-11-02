package com.example.insuranceapi.policy;

import com.example.insuranceapi.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public PolicyResponse findByPolicyNo(String policyNo) {
        Policy policy = policyRepository.findByPolicyNo(policyNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "保险保单不存在, policyNo=" + policyNo
                ));

        return PolicyResponse.from(policy);
    }
}
