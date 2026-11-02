//业务层
package com.example.insuranceapi.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.insuranceapi.common.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final InsuranceProductRepository productRepository;

    public ProductService(InsuranceProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
    return productRepository.findById(id)
            .map(ProductResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "保险产品不存在, id=" + id
            ));
}
}