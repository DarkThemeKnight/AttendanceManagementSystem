package com.backend.FaceRecognition.helper;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.utils.FaceRecognitionEndpoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitializeBeans {
    private final ApplicationUserService applicationUserService;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationService authenticationService;
    private void setupSuperAdmin() {
        log.info("Setting up Super Admin...");
        ApplicationUser user = new ApplicationUser(
                "0001",
                "Omotola",
                "David",
                "Ayanfeoluwa",
                "ayanfeoluwadafidi@outlook.com",
                passwordEncoder.encode("141066"),
                "Demo Address",
                "08055132800",
                Set.of(Role.ROLE_SUPER_ADMIN),
                true,
                true,
                true,
                true,
                null);
        if(applicationUserService.findUser(user.getId()).isEmpty()) {
            applicationUserService.create(user);
        }
        log.info("Super Admin setup complete.");
    }
    @Bean
    public CommandLineRunner setupApplication() {
        return args -> {
                log.info("Setting up application...");
                setupSuperAdmin();
                log.info("Application setup complete.");
        };
    }
    @Value("${faceRecognition.rec}")
    private String recognizeEndpoint;
    @Value("${faceRecognition.ip}")
    private String imageProcessingEndpoint;

    @Bean
    public FaceRecognitionEndpoints initializeEndpoints() {
        log.info("Initializing Face Recognition Endpoints...");
        Map<String, String> endpointMap = new HashMap<>();
        endpointMap.put("ip", imageProcessingEndpoint);
        endpointMap.put("rec", recognizeEndpoint);
        log.info("Face Recognition Endpoints initialized.");
        return new FaceRecognitionEndpoints(endpointMap);
    }


}
