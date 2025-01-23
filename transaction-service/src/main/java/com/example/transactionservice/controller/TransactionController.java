package com.example.transactionservice.controller;

import com.example.transactionservice.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> createTransaction(@PathVariable Long productId, HttpServletRequest request) {
        return ResponseEntity.ok(transactionService.createTransaction(productId));
    }

    @GetMapping(path = "")
    public ResponseEntity<?> getTransactions(HttpServletRequest request) {
        return ResponseEntity.ok("hahahaa");
    }
}
