package com.example.insuranceapi.policy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNo(String policyNo);

    Optional<Policy> findByOrderOrderNo(String orderNo);
}