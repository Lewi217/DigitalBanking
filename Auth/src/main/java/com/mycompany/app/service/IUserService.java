package com.mycompany.app.service;

import com.mycompany.app.dto.*;
import com.mycompany.app.model.User;

import java.util.List;

public interface IUserService {
    User loadUserByUsername(String username);
    User getUserById(Long userId);
    List<User> getAllUsers();
    User createUser(RegisterRequest request);
    LoginResponse logInUser(LoginRequest request);
    User updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);
    UserDto convertUserToDto(User user);
}
