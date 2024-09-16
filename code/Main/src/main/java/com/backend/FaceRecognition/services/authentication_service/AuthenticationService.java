package com.backend.FaceRecognition.services.authentication_service;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.ResetPasswordToken;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.repository.ResetPasswordTokenSaltRepository;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.mail.MailService;
import com.backend.FaceRecognition.utils.ResetPassword;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class AuthenticationService {
    private final ApplicationUserService applicationUserService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;
    private final MailService mailService;
    private final ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository;

    public AuthenticationService(ApplicationUserService applicationUserService, JwtService jwtService,
                                 PasswordEncoder passwordEncoder, StudentService studentService, MailService mailService, ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository) {
        this.applicationUserService = applicationUserService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
        this.mailService = mailService;
        this.resetPasswordTokenSaltRepository = resetPasswordTokenSaltRepository;
    }

    public ResponseEntity<Response> register(ApplicationUserRequest applicationUser, String token) {
        log.info("Registration request received for user: {}", applicationUser.getId());
        ApplicationUser user = buildUser(applicationUser);
        String type = applicationUser.getRole().toLowerCase();

        return switch (type) {
            case "admin" -> {
                log.info("Processing registration as admin");

                String id = jwtService.getId(jwtService.extractTokenFromHeader(token));
                Optional<ApplicationUser> requestUserOpt = applicationUserService.findUser(id);

                if (requestUserOpt.isEmpty() || !requestUserOpt.get().getUserRole().contains(Role.ROLE_SUPER_ADMIN)) {
                    log.warn("Unauthorized attempt to register admin by user with ID: {}", id);
                    yield new ResponseEntity<>(new Response(), HttpStatus.UNAUTHORIZED);
                }

                user.setUserRole(Set.of(Role.ROLE_ADMIN));
                ResponseEntity<Void> response = applicationUserService.create(user);
                if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    log.warn("Registration failed: User already exists for admin role");
                    yield new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
                }
                log.info("Admin user registered successfully");
                yield new ResponseEntity<>(new Response("User Registered Successfully"), HttpStatus.OK);
            }
            case "hardware" -> {
                log.info("Processing registration as hardware user");

                user.setUserRole(Set.of(Role.ROLE_HARDWARE));
                ResponseEntity<Void> response = applicationUserService.create(user);
                if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    log.warn("Registration failed: User already exists for hardware role");
                    yield new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
                }

                String value = jwtService.generate(new HashMap<>(), user, JwtService.getDate(20, 'Y'));
                log.info("Hardware user registered successfully with ID: {}", value);
                yield new ResponseEntity<>(new Response("ID=" + value), HttpStatus.OK);
            }
            case "instructor" -> {
                log.info("Processing registration as instructor");

                user.setUserRole(Set.of(Role.ROLE_LECTURER));
                ResponseEntity<Void> response = applicationUserService.create(user);
                if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    log.warn("Registration failed: User already exists for instructor role");
                    yield new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
                }
                log.info("Instructor user registered successfully");
                yield new ResponseEntity<>(new Response("User Registered Successfully"), HttpStatus.OK);
            }
            case "student" -> {
                log.info("Processing registration as student");

                user.setUserRole(Set.of(Role.ROLE_STUDENT));
                ResponseEntity<Void> response = applicationUserService.create(user);
                if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    log.warn("Registration failed: User already exists for student role");
                    yield new ResponseEntity<>(new Response("User already Exists"), HttpStatus.CONFLICT);
                }

                Student student = buildStudent(applicationUser);
                studentService.saveStudent(student);
                log.info("Student registered and added successfully");
                yield ResponseEntity.ok(new Response("Student Added Successfully"));
            }
            default -> {
                log.warn("Registration failed: Invalid user type: {}", type);
                yield ResponseEntity.badRequest().body(new Response("Bad Type"));
            }
        };
    }

    private String defaultPassword(String lastname) {
        return lastname != null ? passwordEncoder.encode(lastname.toUpperCase()) : passwordEncoder.encode("");
    }

    public Student buildStudent(ApplicationUserRequest request) {
        Student student = new Student();
        student.setFirstname(request.getFirstname());
        student.setLastname(request.getLastname());
        student.setMatriculationNumber(request.getId());
        student.setMiddleName(request.getMiddleName());
        student.setSchoolEmail(request.getSchoolEmail());
        return student;
    }

    private ApplicationUser buildUser(ApplicationUserRequest applicationUser) {
        return ApplicationUser.builder()
                .id(applicationUser.getId())
                .firstname(applicationUser.getFirstname())
                .lastname(applicationUser.getLastname())
                .middleName(applicationUser.getMiddleName())
                .password(defaultPassword(applicationUser.getLastname()))
                .schoolEmail(applicationUser.getSchoolEmail())
                .address(applicationUser.getAddress())
                .phoneNumber(applicationUser.getPhoneNumber())
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isAccountNonExpired(true)
                .isEnabled(true)
                .build();
    }

    public ResponseEntity<AuthenticationResponse> login(AuthenticationRequest request) {
        log.info("Login attempt for user ID: {}", request.getId());

        Optional<ApplicationUser> userOptional = applicationUserService.findUser(request.getId());

        if (userOptional.isEmpty()) {
            log.info("User not found for ID: {}", request.getId());
            return new ResponseEntity<>(new AuthenticationResponse("Invalid Username or Password", null, new HashSet<>()),
                    HttpStatus.NOT_FOUND);
        }

        ApplicationUser user = userOptional.get();
        log.info("User found for ID: {}", user.getId());

        if (!user.isEnabled()) {
            log.warn("Locked account attempting access: {}", request.getId());
            return new ResponseEntity<>(new AuthenticationResponse("Locked Account", null, new HashSet<>()),
                    HttpStatus.LOCKED);
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setCredentialsNonExpired(true);
            ResponseEntity<Void> response = applicationUserService.update(user);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error updating user credentials for ID: {}", user.getId());
                return ResponseEntity.badRequest().body(new AuthenticationResponse("Failed to update user", null, new HashSet<>()));
            }

            Date expiry = JwtService.getDate(1, 'H');
            LocalDateTime localDateTime = LocalDateTime.now().plusHours(12);
            String token = jwtService.generate(new HashMap<>(), user, expiry);

            log.info("Successful login for user ID: {}", request.getId());
            return new ResponseEntity<>(new AuthenticationResponse("Login successfully", token, user.getUserRole(), localDateTime),
                    HttpStatus.OK);
        }

        log.info("Invalid password attempt for user ID: {}", request.getId());
        return new ResponseEntity<>(new AuthenticationResponse("Invalid Username or Password", null, new HashSet<>()),
                HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Void> logout(String bearerToken) {
        String id = jwtService.getId(jwtService.extractTokenFromHeader(bearerToken));
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isPresent()) {
            ApplicationUser user = userOptional.get();
            user.setCredentialsNonExpired(false);
            applicationUserService.update(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Response> forgotPassword(String id) {
       Response response = mailService.sendForgotPasswordResetLink(id);
       if (response.getMessage().equalsIgnoreCase(HttpStatus.NOT_FOUND.name())){
           return new ResponseEntity<>(new Response("User not found"),HttpStatus.NOT_FOUND);
       }
       return ResponseEntity.ok(new Response("Email Sent Successfully"));
    }

    public ResponseEntity<Response> resetPassword(String token, ResetPassword resetPassword) {
        Optional<ResetPasswordToken> resetPasswordTokenOptional = resetPasswordTokenSaltRepository.findBySalt(token);
        if (resetPasswordTokenOptional.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        ResetPasswordToken val = resetPasswordTokenOptional.get();
        if (LocalDateTime.now().isAfter(val.getExpiryDateTime())){
            return  ResponseEntity.badRequest().body(new Response("Link Expired"));
        }
        resetPasswordTokenSaltRepository.delete(val);
        return applicationUserService.resetPassword(val.getUserId(),resetPassword);
    }
    public ResponseEntity<Response> updatePassword(String bearer, ResetPassword resetPassword) {
        jwtService.extractTokenFromHeader(bearer);
        String userId = jwtService.getId(jwtService.extractTokenFromHeader(bearer));
        ApplicationUser applicationUser = applicationUserService.findUser(userId).orElse(null);
        if (applicationUser == null){
            return ResponseEntity.badRequest().build();
        }
        if (passwordEncoder.matches(resetPassword.getOldPassword(),applicationUser.getPassword())){
            applicationUser.setPassword(passwordEncoder.encode(resetPassword.getNewPassword()));
            applicationUserService.update(applicationUser);
            return ResponseEntity.ok(new Response("Updated Successfully"));
        }
        return ResponseEntity.ok(new Response("User does not exist"));
    }
}