//初始化数据配置
package com.example.insuranceapi.config;

import com.example.insuranceapi.product.InsuranceProduct;
import com.example.insuranceapi.product.InsuranceProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeProducts(
            InsuranceProductRepository productRepository
    ) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            productRepository.saveAll(List.of(
                    new InsuranceProduct(
                            "TRAVEL_BASIC",
                            "基础旅行意外险",
                            "提供旅行期间的意外身故及伤残保障",
                            new BigDecimal("100000.00"),
                            new BigDecimal("19.90"),
                            7
                    ),
                    new InsuranceProduct(
                            "TRAVEL_PLUS",
                            "升级旅行意外险",
                            "提供更高保额的旅行意外保障",
                            new BigDecimal("300000.00"),
                            new BigDecimal("49.90"),
                            7
                    )
            ));
        };
    }
}