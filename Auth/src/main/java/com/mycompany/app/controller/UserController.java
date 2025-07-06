package com.mycompany.app.controller;

import com.mycompany.app.dto.UserDto;
import com.mycompany.app.dto.UserUpdateRequest;
import com.mycompany.app.exceptions.CustomExceptionResponse;
import com.mycompany.app.model.User;
import com.mycompany.app.response.ApiResponse;
import com.mycompany.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/get_by_id/{userId}")
    public ResponseEntity<ApiResponse<Collection<E>>> getUserById(@PathVariable("userId") Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(new ApiResponse<Collection<E>>(REQUEST_SUCCESS_MESSAGE, userService.convertUserToDto(user)));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse<Collection<E>>(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<ApiResponse<Collection<E>>> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateRequest request) {
        try {
            User user = userService.updateUser(request, userId);
            return ResponseEntity.ok(new ApiResponse<Collection<E>>(REQUEST_SUCCESS_MESSAGE, userService.convertUserToDto(user)));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse<Collection<E>>(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponse<Collection<E>>> deleteUser(@PathVariable("userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse<Collection<E>>(REQUEST_SUCCESS_MESSAGE, null));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse<Collection<E>>(REQUEST_ERROR_MESSAGE, e.getMessage()));
        }
    }
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
        User user = userService.loadUserByUsername(email);
        UserDto dto = userService.convertUserToDto(user);
        return ResponseEntity.ok(dto);
    }
}
