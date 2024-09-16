package com.backend.FaceRecognition.security;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.repository.ApplicationUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@Slf4j
public class SecurityBeans {
    private final ApplicationUserRepository userRepository;

    public SecurityBeans(ApplicationUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(id -> {
            Optional<ApplicationUser> applicationUser = userRepository.findById(id);
            if (applicationUser.isEmpty()) {
                throw new UsernameNotFoundException("User " + id + " is not found");
            }
            ApplicationUser user = applicationUser.get();
            System.out.println("User => " + user.getFirstname() + " authorities " + user.getAuthorities());
            return user;
        });
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

}
