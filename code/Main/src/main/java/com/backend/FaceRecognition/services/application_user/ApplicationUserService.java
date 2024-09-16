package com.backend.FaceRecognition.services.application_user;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.repository.ApplicationUserRepository;
import com.backend.FaceRecognition.utils.ResetPassword;
import com.backend.FaceRecognition.utils.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationUserService {
    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder encoder;

    public Optional<ApplicationUser> findUser(String userId) {
        log.info("Finding user with ID: {}", userId);
        return applicationUserRepository.findById(userId);
    }

    public java.util.List<ApplicationUser> findAllUsers() {
        log.info("Finding all users");
        return applicationUserRepository.findAll();
    }

    @Transactional
    public ResponseEntity<Void> create(ApplicationUser applicationUser) {
        log.info("Creating user with ID: {}", applicationUser.getId());
        Optional<ApplicationUser> userOptional = applicationUserRepository.findById(applicationUser.getId());
        if (userOptional.isPresent()) {
            log.warn("User with ID: {} already exists", applicationUser.getId());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        applicationUserRepository.save(applicationUser);
        log.info("User with ID: {} created successfully", applicationUser.getId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<Void> update(ApplicationUser appUser) {
        log.info("Updating user with ID: {}", appUser.getId());
        Optional<ApplicationUser> user = applicationUserRepository.findById(appUser.getId());
        if (user.isPresent()) {
            applicationUserRepository.save(appUser);
            log.info("User with ID: {} updated successfully", appUser.getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        log.warn("User with ID: {} not found", appUser.getId());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Transactional
    public ResponseEntity<Response> resetPassword(String userId, ResetPassword resetPassword) {
        log.info("Resetting password for user with ID: {}", userId);
        ApplicationUser applicationUser = applicationUserRepository.findById(userId).orElse(null);
        if (applicationUser == null) {
            log.warn("User with ID: {} not found", userId);
            return ResponseEntity.notFound().build();
        }
        applicationUser.setPassword(encoder.encode(resetPassword.getNewPassword()));
        applicationUserRepository.save(applicationUser);
        log.info("Password for user with ID: {} changed successfully", userId);
        return ResponseEntity.ok(new Response("Password changed successfully"));
    }
}
