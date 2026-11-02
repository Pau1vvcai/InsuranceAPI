//数据库访问接口
package com.example.insuranceapi.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsuranceProductRepository
        extends JpaRepository<InsuranceProduct, Long> {

    Optional<InsuranceProduct> findByCode(String code);
}