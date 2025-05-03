package com.mycompany.app.service;

import com.mycompany.app.AppUtils;
import com.mycompany.app.dto.*;
import com.mycompany.app.exceptions.CustomExceptionResponse;
import com.mycompany.app.model.User;
import com.mycompany.app.repository.UserRepository;
import com.mycompany.app.SecurityConfigs.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public User loadUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new CustomExceptionResponse("User not found with email: " + username));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomExceptionResponse("user not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(RegisterRequest request) {
        return Optional.of(request).filter(user -> !userRepository.existsByEmail(request.getEmail())).map(req -> {
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));  // Password encoding
            return userRepository.save(user);

        }).orElseThrow(() -> new CustomExceptionResponse("Oops!" + request.getEmail() + " already exists") );
    }

    @Override
    public LoginResponse logInUser(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            HashMap<String, Object> claims = new HashMap<>();
            String refreshToken = jwtUtil.generateRefreshToken(claims, userDetails);
            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                    ()-> new CustomExceptionResponse("User Not found")
            );
            UserDto userDto = convertUserToDto(user);

            return LoginResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .user(userDto)
                    .build();
        } catch (AuthenticationException | CustomExceptionResponse e) {
            throw new CustomExceptionResponse(e.getMessage());
        }
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        return userRepository.findById(userId).map(existingUser ->{
            AppUtils.updateField(existingUser::setFirstName, request.getFirstname(), existingUser.getFirstName());
            AppUtils.updateField(existingUser::setLastName,request.getLastname(), existingUser.getLastName());
            return userRepository.save(existingUser);
        }).orElseThrow(()->new CustomExceptionResponse("User not found!"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresentOrElse(userRepository::delete, () -> {
            throw new CustomExceptionResponse("User not found");
        });
    }

    @Override
    public UserDto convertUserToDto(User user){
        return mapper.map(user, UserDto.class);
    }
}
