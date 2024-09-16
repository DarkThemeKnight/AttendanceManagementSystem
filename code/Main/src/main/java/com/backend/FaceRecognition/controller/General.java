package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.helper.Utility;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.entities.Notification;
import com.backend.FaceRecognition.repository.NotificationRepository;
import com.backend.FaceRecognition.services.extras.ScheduleService;
import com.backend.FaceRecognition.utils.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/general")
@Component
public class General {
    private final StudentService studentService;
    private final NotificationRepository notificationRepository;
    private final ScheduleService scheduleService;
    private final JwtService jwtService;
    private final ApplicationUserService applicationUserService;
    private final Utility utility;


    public General(StudentService studentService, NotificationRepository notificationRepository, ScheduleService scheduleService,
                   JwtService jwtService, ApplicationUserService applicationUserService, Utility utility) {
        this.studentService = studentService;
        this.notificationRepository = notificationRepository;
        this.scheduleService = scheduleService;
        this.applicationUserService = applicationUserService;
        this.jwtService = jwtService;
        this.utility = utility;
    }
    @GetMapping("/notification")
    public ResponseEntity<NotificationResponse> getMyNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        List<NotificationResponse.CustomData> data = notifications.stream()
                .map(notification -> new NotificationResponse.CustomData(notification.getTitle(),
                        notification.getContent()))
                .toList();
        NotificationResponse response = new NotificationResponse("successful", data);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/schedule")
    public ResponseEntity<ScheduleSetupResponse> getMySchedule(@RequestHeader("Authorization") String bearer) {
        return scheduleService.fetch(bearer);
    }
    @GetMapping("/schedule/{day}")
    public ResponseEntity<ScheduleSetupResponse> getMySchedule(@RequestHeader("Authorization") String bearer, @PathVariable("day") String day) {
        return scheduleService.fetch(bearer, day);
    }
    @PostMapping("/schedule")
    public ResponseEntity<ScheduleSetupResponse> addSchedule(@RequestBody ScheduleSetupRequest scheduleRequest,
            @RequestHeader("Authorization") String auth) {
        return scheduleService.setupMySchedule(scheduleRequest, auth);
    }
    @GetMapping
    public ResponseEntity<ApplicationUser> whoAmI(@RequestHeader("Authorization") String bearer) {
        String userId = jwtService.getId(jwtService.extractTokenFromHeader(bearer));
        return applicationUserService.findUser(userId).stream().findFirst().map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/studentProfile")
    public ResponseEntity<StudentProfile> getProfile(@RequestParam String studentId){
        return studentService.getMyProfile(studentId);
    }
    @PreAuthorize("hasAnyRole('ROLE_LECTURER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @GetMapping("/attendance/stats")
    public ResponseEntity<AttendanceStatsDTO> generateAttendanceStatistics(@RequestParam(required = false) String... subjects) {
        return utility.generateAttendanceStatistics(subjects);
    }

}