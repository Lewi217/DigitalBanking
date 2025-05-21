package com.mycompany.app.client;

import com.mycompany.app.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "auth-service",
        configuration = FeignClientConfig.class
)
public interface AuthServiceClient {
    @GetMapping("/api/v1/users/get_by_id/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/users/by-email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}