package com.backend.FaceRecognition.security;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.repository.ApplicationUserRepository;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ApplicationUserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, ApplicationUserRepository userService) {
        this.jwtService = jwtService;
        this.userRepository = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String userId;

        log.debug("Request received with Authorization header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (authHeader == null) {
                log.info("Null Token");
            } else {
                log.info("Token does not start with \"Bearer \"");
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            token = authHeader.substring(7);
            log.debug("Token extracted: {}", token);
            userId = jwtService.getId(token);
            log.debug("User ID extracted from token: {}", userId);

            if (!userId.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                ApplicationUser applicationUser = userRepository.findById(userId).orElse(null);

                if (applicationUser == null) {
                    log.warn("User not found for ID: {}", userId);
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Username not found");
                    return;
                }

                log.debug("User found: {}", applicationUser);

                if (!applicationUser.isEnabled()) {
                    log.warn("User account is disabled: {}", userId);
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Disabled Account");
                    return;
                }

                if (!applicationUser.isCredentialsNonExpired()) {
                    log.warn("User credentials are expired: {}", userId);
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Expired credentials");
                    return;
                }

                if (jwtService.isValidToken(token, applicationUser)) {
                    log.debug("Token is valid for user: {}", userId);
                    SecurityContext context = SecurityContextHolder.createEmptyContext();

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(applicationUser, null, applicationUser.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(context);

                    log.debug("Authentication set in security context for user: {}", userId);
                }
            }
        } catch (MalformedJwtException e) {
            log.error("Malformed Token: {}", e.getMessage());
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Malformed Jwt Exception");
        } catch (Exception e) {
            log.error("Error occurred during token processing: {}", e.getMessage(), e);
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Exception occur " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
