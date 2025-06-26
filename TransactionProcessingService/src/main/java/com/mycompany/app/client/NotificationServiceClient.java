package com.mycompany.app.client;

import com.mycompany.app.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    @PostMapping("/api/v1/notifications/transaction")
    ApiResponse sendTransactionNotification(
            @RequestParam("userId") String userId,
            @RequestParam("transactionType") String transactionType,
            @RequestParam("amount") String amount,
            @RequestParam("accountNumber") String accountNumber
    );
}
