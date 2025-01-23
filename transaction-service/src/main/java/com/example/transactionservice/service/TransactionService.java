package com.example.transactionservice.service;

import com.example.transactionservice.client.ProductServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private ProductServiceClient productServiceClient;

    public Map<String, Object> createTransaction(Long productId) {
        Map<String, Object> product = productServiceClient.getProduct(productId);
        return Map.of("product", product, "status", "SUCCESS");
    }
}
