//订单数据库访问接口
package com.example.insuranceapi.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsuranceOrderRepository
        extends JpaRepository<InsuranceOrder, Long> {

    Optional<InsuranceOrder> findByOrderNo(String orderNo);
}