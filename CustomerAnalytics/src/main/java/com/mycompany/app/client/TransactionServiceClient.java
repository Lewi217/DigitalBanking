package com.mycompany.app.client;

import com.mycompany.app.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

@FeignClient(name = "transaction-service", path = "/internal/transactions")
public interface TransactionServiceClient {

    @GetMapping("/history/{accountId}")
    ApiResponse getTransactionHistory(@PathVariable String accountId);

    @GetMapping("/user/{userId}")
    ApiResponse getUserTransactions(@PathVariable String userId);

    @GetMapping("/search")
    ApiResponse searchTransactions(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) String userId
    );
}
