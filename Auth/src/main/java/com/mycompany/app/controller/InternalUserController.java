package com.mycompany.app.controller;

import com.mycompany.app.dto.UserDto;
import com.mycompany.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final IUserService userService;

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable("userId") Long userId) {
        try {
            return userService.convertUserToDto(userService.getUserById(userId));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found with ID: " + userId, e
            );
        }
    }

    @GetMapping("/email/{email}")
    public UserDto getUserByEmail(@PathVariable("email") String email) {
        try {
            return userService.convertUserToDto(userService.loadUserByUsername(email));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found with email: " + email, e
            );
        }
    }

    @GetMapping("/{userId}/exists")
    public boolean userExists(@PathVariable("userId") Long userId) {
        try {
            userService.getUserById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
