package com.example.transactionservice.client;

import com.example.transactionservice.config.FeignConfigDimanic;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "product-service", url = "http://localhost:8081", configuration = FeignConfigDimanic.class)
public interface ProductServiceClient {

    @GetMapping("/products/{id}")
    Map<String, Object> getProduct(@PathVariable("id") Long productId);
}