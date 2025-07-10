package com.mycompany.app.client;

import com.mycompany.app.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", path = "/internal/users")
public interface AuthServiceClient {

    @GetMapping("/{userId}")
    ApiResponse getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/username/{username}")
    ApiResponse getUserByUsername(@PathVariable("username") String username);
}
