package com.backend.FaceRecognition.services.authorization_service.super_admin;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
@Slf4j
@Service
public class SuperUserService {
    private final ApplicationUserService applicationUserService;

    public SuperUserService(ApplicationUserService applicationUserService) {
        this.applicationUserService = applicationUserService;
    }

    public ResponseEntity<Response> setToAdmin(String id) {
        log.info("Request to update user ID: {} to ADMIN role", id);

        Optional<ApplicationUser> response = applicationUserService.findUser(id);

        if (response.isPresent()) {
            ApplicationUser user = response.get();
            log.info("User found with ID: {}", id);

            boolean added = user.addUserRole(Role.ROLE_ADMIN);

            if (added) {
                applicationUserService.update(user);
                log.info("User ID: {} updated to ADMIN role successfully", id);
                return new ResponseEntity<>(new Response("USER UPDATED TO ADMIN"), HttpStatus.OK);
            } else {
                log.warn("User ID: {} is already an ADMIN", id);
                return new ResponseEntity<>(new Response("ALREADY AN ADMIN"), HttpStatus.CONFLICT);
            }
        }

        log.warn("User not found with ID: {}", id);
        return new ResponseEntity<>(new Response("USER NOT FOUND"), HttpStatus.NOT_FOUND);
    }
}
