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

import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_ERROR_MESSAGE;
import static com.mycompany.app.exceptions.ApiResponseUtils.REQUEST_SUCCESS_MESSAGE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;
    @GetMapping("/get_by_id/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId){
        try {
            User user = userService.getUserById(userId);
            UserDto userDto= userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,userDto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(REQUEST_ERROR_MESSAGE,e.getMessage()));
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserUpdateRequest request, @PathVariable Long userId){
        try {
            User user = userService.updateUser(request, userId);
            UserDto userDto= userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,userDto));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(REQUEST_ERROR_MESSAGE,e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId){
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse(REQUEST_SUCCESS_MESSAGE,null));
        } catch (CustomExceptionResponse e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(REQUEST_ERROR_MESSAGE,e.getMessage()));
        }
    }
}
