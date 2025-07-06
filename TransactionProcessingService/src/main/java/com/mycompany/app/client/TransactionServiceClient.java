package com.mycompany.app.client;

import com.mycompany.app.dto.TransactionRequest;
import com.mycompany.app.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;

@FeignClient(
        name = "transaction-service",
        configuration = FeignClientConfig.class
)
public interface TransactionServiceClient {

    @PostMapping("/internal/transactions/create")
    ApiResponse createTransaction(@RequestBody TransactionRequest request);

    @GetMapping("/internal/transactions/history/{accountId}")
    ApiResponse getTransactionHistory(@PathVariable("accountId") Long accountId);

    @GetMapping("/internal/transactions/search")
    ApiResponse searchTransactions(@RequestParam("from") Instant from,
                                   @RequestParam("to") Instant to);

    @GetMapping("/internal/transactions/account/{accountId}/balance")
    ApiResponse getAccountBalance(@PathVariable("accountId") Long accountId);
}
