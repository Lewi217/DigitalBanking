package com.mycompany.app.security;

import com.mycompany.app.client.AuthServiceClient;
import com.mycompany.app.dto.UserDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@Configuration
public class UserDetailsConfig {

    private final AuthServiceClient authServiceClient;

    public UserDetailsConfig(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserDto user = authServiceClient.getUserByEmail(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            // Create UserDetails instance
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    "", // password is not needed for JWT verification
                    List.of(new SimpleGrantedAuthority("ROLE_USER")) // Adjust if you have roles
            );
        };
    }
}
