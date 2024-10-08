package com.backend.FaceRecognition.services.authentication_service;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.ResetPasswordToken;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.helper.Utility;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

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
                    yield new ResponseEntity<>(new Response("Unauthorized to Register user with id-> "+applicationUser.getId()), HttpStatus.UNAUTHORIZED);
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

    public ResponseEntity<Response> register(MultipartFile file, String token) {
        String filename = file.getOriginalFilename();
        log.info("Filename {}",filename);
        if (filename == null) {
            return ResponseEntity.badRequest().body(new Response("Filename is null"));
        }
        if (!filename.endsWith(".csv")) {
            return ResponseEntity.badRequest().body(new Response("Filename is not a CSV file"));
        }

        List<ApplicationUserRequest> toRegister = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 6) {
                    validationErrors.add("Insufficient data for line: " + line);
                    continue; // Skip this line if not enough tokens
                }
                String id = tokens[0];
                String firstname = tokens[1];
                String lastname = tokens[2];
                String email = tokens[3];
                String phoneNumber = tokens[4];
                String role = tokens[5];

                String validationMessage = validateInput(id, firstname, lastname, email, phoneNumber);
                if (!"Validation successful!".equals(validationMessage)) {
                    validationErrors.add(validationMessage + " for user -> " + id + " " + firstname + " " + lastname + " " + email + " " + phoneNumber);
                    continue; // Skip registration for this invalid entry
                }

                ApplicationUserRequest applicationUserRequest = ApplicationUserRequest.builder()
                        .firstname(firstname)
                        .lastname(lastname)
                        .phoneNumber(phoneNumber)
                        .schoolEmail(email)
                        .id(id)
                        .role(role)
                        .build();

                ApplicationUser applicationUser = applicationUserService.findUser(id).orElse(null);
                if (applicationUser == null) {
                    toRegister.add(applicationUserRequest);
                }
            }
        } catch (IOException ex) {
            log.error("Error ",ex);
            return ResponseEntity.badRequest().body(new Response("Error Processing file: " + ex.getMessage()));
        }

        // If there were any validation errors, return them
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(new Response(String.join("\n", validationErrors)));
        }
        // Process registration
        StringBuilder sb = new StringBuilder();
        for (ApplicationUserRequest applicationUserRequest : toRegister) {
            ResponseEntity<Response> response = register(applicationUserRequest, token);
            if (!response.getStatusCode().is2xxSuccessful()) {
                sb.append(response.getBody().getMessage()).append("\n");
            }
        }
        return ResponseEntity.ok(new Response(sb.toString()));
    }
    public static String validateInput(String id, String firstname, String lastname, String email, String phoneNumber) {
        StringBuilder validationMessage = new StringBuilder();

        // Validate ID
        if (id == null  || (id.length() < 10 || id.length() > 11)) {
            assert id != null;
            log.info("Id Length {}",id.length());
            validationMessage.append("ID must be 10-11 alphanumeric characters.\n");
        }

        // Validate First Name
        if (firstname == null || firstname.trim().isEmpty()) {
            validationMessage.append("First name must be non-null and non-empty.\n");
        }

        // Validate Last Name
        if (lastname == null || lastname.trim().isEmpty()) {
            validationMessage.append("Last name must be non-null and non-empty.\n");
        }

        // Validate Email
        if (email == null || !isValidEmail(email)) {
            validationMessage.append("Email must be non-null and in a valid format.\n");
        }

        // Validate Phone Number
        if (phoneNumber == null || !isValidPhoneNumber(phoneNumber)) {
            validationMessage.append("Phone number must be non-null and exactly 11 digits.\n");
        }

        return validationMessage.length() == 0 ? "Validation successful!" : validationMessage.toString();
    }

    private static boolean isAlphanumeric(String str) {
        return str.matches("[a-zA-Z0-9]+");
    }

    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{11}");
    }



}