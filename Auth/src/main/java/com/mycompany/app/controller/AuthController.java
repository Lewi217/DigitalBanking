package com.mycompany.app.controller;

import com.mycompany.app.dto.*;
import com.mycompany.app.exceptions.CustomExceptionResponse;
import com.mycompany.app.model.User;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.security.JwtUtil;
import com.mycompany.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final IUserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request);
            UserDto userDto= userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,userDto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(REQUEST_ERROR_MESSAGE,e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.logInUser(request);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,response));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(FORBIDDEN).body(new ApiResponse(REQUEST_ERROR_MESSAGE,e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            if (request.getRefreshToken() == null || jwtUtil.isTokenExpired(request.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Invalid or expired refresh token"));
            }

            String username = jwtUtil.extractUsername(request.getRefreshToken());
            User user = userService.loadUserByUsername(username);

            UserDetails userDetails = jwtUtil.toUserDetails(user);

            if (!jwtUtil.isTokenValid(request.getRefreshToken(), userDetails)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(REQUEST_ERROR_MESSAGE, "Invalid refresh token"));
            }

            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(new HashMap<>(), userDetails);

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .user(userService.convertUserToDto(user))  // Convert user to DTO
                    .build();

            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE, loginResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

}
