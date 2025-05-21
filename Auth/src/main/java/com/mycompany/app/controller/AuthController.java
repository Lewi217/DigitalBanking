package com.mycompany.app.controller;

import com.mycompany.app.dto.*;
import com.mycompany.app.exceptions.CustomExceptionResponse;
import com.mycompany.app.model.User;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.SecurityConfigs.JwtUtil;
import com.mycompany.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final IUserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, userService.convertUserToDto(user)));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.logInUser(request);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, response));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || jwtUtil.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(FORBIDDEN)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Invalid or expired refresh token"));
            }

            String username = jwtUtil.extractUsername(refreshToken);
            User user = userService.loadUserByUsername(username);
            if (!jwtUtil.isTokenValid(refreshToken, jwtUtil.toUserDetails(user))) {
                return ResponseEntity.status(FORBIDDEN)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Invalid refresh token"));
            }

            String newAccessToken = jwtUtil.generateToken(jwtUtil.toUserDetails(user));
            String newRefreshToken = jwtUtil.generateRefreshToken(new HashMap<>(), jwtUtil.toUserDetails(user));

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .user(userService.convertUserToDto(user))
                    .build();

            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, loginResponse));
        } catch (Exception e) {
            return ResponseEntity.status(FORBIDDEN)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }
}
