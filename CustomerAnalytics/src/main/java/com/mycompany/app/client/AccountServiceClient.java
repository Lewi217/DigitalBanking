package com.mycompany.app.client;

import com.mycompany.app.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "account-service", path = "/internal/accounts")
public interface AccountServiceClient {

    @GetMapping("/user/{userId}")
    ApiResponse getUserAccounts(@PathVariable Long userId);

    @GetMapping("/{accountId}")
    ApiResponse getAccountById(@PathVariable Long accountId);
}
