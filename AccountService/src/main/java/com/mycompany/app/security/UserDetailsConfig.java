package com.mycompany.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // For JWT validation, we don't need to fetch user details from database
            // The JWT token itself contains the necessary information
            // We just create a minimal UserDetails object for Spring Security
            return User.builder()
                    .username(username)
                    .password("") // Password not needed for JWT validation
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();
        };
    }
}