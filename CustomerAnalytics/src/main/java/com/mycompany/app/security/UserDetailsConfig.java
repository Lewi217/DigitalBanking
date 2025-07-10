package com.mycompany.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            return User.builder()
                    .username(username)
                    .password("") // Password not needed for JWT validation
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();
        };
    }
}
