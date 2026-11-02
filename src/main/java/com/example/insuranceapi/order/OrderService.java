package com.example.insuranceapi.order;

import com.example.insuranceapi.common.ResourceNotFoundException;
import com.example.insuranceapi.product.InsuranceProduct;
import com.example.insuranceapi.product.InsuranceProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private final InsuranceOrderRepository orderRepository;
    private final InsuranceProductRepository productRepository;

    public OrderService(
            InsuranceOrderRepository orderRepository,
            InsuranceProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        InsuranceProduct product = productRepository
                .findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "保险产品不存在, id=" + request.productId()
                ));

        String orderNo = generateOrderNo();

        InsuranceOrder order = new InsuranceOrder(
                orderNo,
                product,
                request.applicant().name(),
                request.applicant().idCard(),
                request.applicant().phone(),
                request.insuredPerson().name(),
                request.insuredPerson().idCard(),
                request.insuredPerson().relationship()
        );

        InsuranceOrder savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    private String generateOrderNo() {
        return "ORD-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase();
    }
    @Transactional(readOnly = true)

    public OrderResponse findByOrderNo(String orderNo) {
        InsuranceOrder order = orderRepository
            .findByOrderNo(orderNo)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "保险订单不存在, orderNo=" + orderNo
            ));

        return OrderResponse.from(order);
    }
}