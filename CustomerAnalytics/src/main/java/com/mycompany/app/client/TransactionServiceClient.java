package com.mycompany.app.client;

import com.mycompany.app.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;


@FeignClient(name = "transaction-service", url = "${account-service.url}", path = "/internal/transactions" ,configuration = FeignClientConfig.class)
public interface TransactionServiceClient {

    @GetMapping("/history/{accountId}")
    ApiResponse getTransactionHistory(@PathVariable("accountId") String accountId);

    @GetMapping("/user/{userId}")
    ApiResponse getUserTransactions(@PathVariable("userId") String userId);
    @GetMapping("/search")
    ApiResponse searchTransactions(@RequestParam("from") Instant from, @RequestParam("to") Instant to, @RequestParam(value = "userId", required = false) String userId
    );
}

