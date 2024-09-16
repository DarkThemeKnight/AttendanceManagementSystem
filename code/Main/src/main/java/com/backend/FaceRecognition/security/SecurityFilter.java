package com.backend.FaceRecognition.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityFilter(AuthenticationManager authenticationManager, JwtAuthFilter jwtAuthFilter) {
        this.authenticationManager = authenticationManager;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public DefaultSecurityFilterChain securityFilterChain(HttpSecurity security) {

        DefaultSecurityFilterChain filterChain = null;
        try {
            filterChain = security.csrf(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(managementConfigure -> managementConfigure.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                            authorizationManagerRequestMatcherRegistry
                                    .requestMatchers("api/v1/auth/**")
                                    .permitAll()
                                    .requestMatchers("test")
                                    .permitAll()
                                    .requestMatchers("api/v1/admin/**")
                                    .hasAnyAuthority("ROLE_ADMIN","ROLE_SUPER_ADMIN")
                                    .requestMatchers("api/v1/advisor/**")
                                    .hasAnyAuthority("ROLE_ADVISOR","ROLE_ADMIN","ROLE_SUPER_ADMIN")
                                    .requestMatchers("api/v1/attendance/**")
                                    .hasAuthority("ROLE_LECTURER")
                                    .requestMatchers("api/v1/general/**","/api/v1/profile-picture/**")
                                    .authenticated()
                                    .requestMatchers("api/v1/encodings/**")
                                    .permitAll()
                                    .requestMatchers("/api/v1/students/update")
                                    .permitAll()
                                    .requestMatchers("/api/v1/students/image")
                                    .authenticated()
                                    .requestMatchers("api/v1/super-admin/**")
                                    .hasRole("SUPER_ADMIN")
                                    .requestMatchers("api/v1/hardware/**")
                                    .hasAuthority("ROLE_HARDWARE")
                                    .requestMatchers("test/**")
                                    .permitAll()
                                    .anyRequest()
                                    .denyAll()
                    )
                    .authenticationManager(authenticationManager)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(
                            httpSecurityExceptionHandlingConfigurer ->
                                    httpSecurityExceptionHandlingConfigurer.
                                            authenticationEntryPoint(
                                                    (request, response, authException) ->{
                                                        log.error("Unauthorized error: cant access resource");
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                                    }
                                            )
                                            .accessDeniedHandler(
                                                    (request, response, accessDeniedException) ->{
                                                        log.error("Access denied error");
                                                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                                                    }
                                            )
                    )
                    .build();
        }catch (Exception e) {
            log.error("Exception in filter Security filter chain");
            throw new RuntimeException  (e);
        }
        return filterChain;
    }

}
