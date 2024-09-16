package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Notification;
import com.backend.FaceRecognition.repository.NotificationRepository;
import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.services.authorization_service.admin.AdminService;
import com.backend.FaceRecognition.services.authorization_service.super_admin.SuperUserService;
import com.backend.FaceRecognition.utils.GetListOfUsers;
import com.backend.FaceRecognition.utils.NotificationRequest;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.subject.AllSubjects;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@CrossOrigin("*")
@Slf4j
@RequestMapping("api/v1/admin")
@Component
public class AdminController {
    private final AdminService adminService;
    private final AuthenticationService authenticationService;
    private final NotificationRepository notificationRepository;
    private final SuperUserService superUserService;

    public AdminController(AdminService adminService, AuthenticationService authenticationService,
                           NotificationRepository notificationRepository, SuperUserService superUserService) {
        this.adminService = adminService;
        this.authenticationService = authenticationService;
        this.notificationRepository = notificationRepository;
        this.superUserService = superUserService;
    }

    @PostMapping("/notification")
    public ResponseEntity<Response> notification(@RequestBody NotificationRequest request) {
        log.info("Received notification request: {}", request);

        try {
            Notification notification = new Notification();
            notification.setTitle(request.getTitle());
            notification.setContent(request.getContent());
            notification.setValidUntil(LocalDate.parse(request.getValidUntil()));

            log.debug("Saving notification: {}", notification);
            notificationRepository.save(notification);
            log.info("Notification saved successfully");

            return ResponseEntity.ok(new Response("Notification Saved"));
        } catch (DateTimeParseException e) {
            log.error("Failed to parse date: {}", request.getValidUntil(), e);
            return ResponseEntity.badRequest().body(new Response("Invalid date type must be yyyy-MM-dd"));
        } catch (Exception e) {
            log.error("An unexpected error occurred", e);
            return ResponseEntity.status(500).body(new Response("An error occurred while saving the notification"));
        }
    }

    @PutMapping("/update-subject")
    public ResponseEntity<Response> updateSubject(@RequestBody SubjectRequest request) {
        return build(adminService.updateSubject(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Response> register(
            @RequestHeader("Authorization") String token,
            @RequestBody ApplicationUserRequest applicationUser
    ) {
        log.info("Register User {}",applicationUser);
        return authenticationService.register(applicationUser, token);
    }
    @PostMapping("/set-to-admin")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Response> setToAdmin(@RequestParam String id) {
        return superUserService.setToAdmin(id);
    }
    @GetMapping("/{code}")
    public ResponseEntity<GetListOfUsers> getAll(@RequestHeader("Authorization") String bearer,@PathVariable("code") String code) {
        return adminService.getAll(code.toLowerCase(),bearer);
    }
    @GetMapping("/get-user")
    public ResponseEntity<ApplicationUser> getUser(@RequestHeader("Authorization") String bearer,@RequestParam("id") String id){
        return adminService.getUser(id,bearer);
    }
    @PostMapping("/lock-account")
    public ResponseEntity<Response> lockAccount(@RequestParam("id") String id,
            @RequestHeader("Authorization") String bearer) {
        return build(adminService.lockAccount(id, bearer));
    }
    @PostMapping("/unlock-account")
    public ResponseEntity<Response> unlockAccount(@RequestParam("id") String id,
            @RequestHeader("Authorization") String bearer) {
        return build(adminService.unlockAccount(id, bearer));
    }
    @PostMapping("/add-subject")
    public ResponseEntity<Response> addSubject(@RequestBody SubjectRequest request) {
        request.setSubjectCode(request.getSubjectCode().toUpperCase());
        return build(adminService.addSubject(request));
    }
    @DeleteMapping("/subject")
    public ResponseEntity<Response> deleteSubject(@RequestParam("id") String subjectId) {
        return build(adminService.deleteSubject(subjectId));
    }

    private ResponseEntity<Response> build(ResponseEntity<String> response) {
        return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());
    }
    @GetMapping("/subject/{subjectCode}")
    public ResponseEntity<SubjectResponse> getSubject(@PathVariable String subjectCode) {
        return adminService.getSubject(subjectCode);
    }
    @GetMapping("/subject")
    public ResponseEntity<AllSubjects> getSubjects(@RequestParam("student") String student){
        return adminService.getAllSubject(Boolean.parseBoolean(student));
    }


}
