package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.attendance_service.AttendanceService;

import com.backend.FaceRecognition.services.authorization_service.lecturer_service.LecturerService;
import com.backend.FaceRecognition.utils.*;
import com.backend.FaceRecognition.utils.history.AttendanceRecordHistoryResponse;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/attendance")
@Component
@Slf4j
public class LecturerController {
    private final AttendanceService attendanceService;
    private final LecturerService lecturerService;

    public LecturerController(AttendanceService attendanceService, LecturerService lecturerService) {
        this.attendanceService = attendanceService;
        this.lecturerService = lecturerService;
    }
    @GetMapping
    public ResponseEntity<SubjectResponse> getSubject(@RequestParam String subjectCode,@RequestHeader("Authorization") String bearer) {
        return lecturerService.getSubject(subjectCode,bearer);
    }
    @GetMapping("/mySubjects")
    public ResponseEntity<ListOfSubjects> getSubjects(@RequestHeader("Authorization") String auth) {
        log.info("Received request to get subjects with authorization header: {}", auth);
        return lecturerService.getSubjectList(auth);
    }
    @PostMapping("/initialize")
    public ResponseEntity<InitializeAttendanceResponse> initializeAttendance(
            @RequestBody InitializeAttendance initializeAttendance,
            HttpServletRequest request) {
        log.info("initialize attendance request {}",initializeAttendance);
        try {
            return attendanceService.initializeAttendance(initializeAttendance.getSubjectCode(),
                    request.getHeader("Authorization"), initializeAttendance.getDuration());
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/record")
    public ResponseEntity<AttendanceRecordResponse> getRecord(@RequestParam String subjectCode,
            @RequestParam String date,
            @RequestParam("sort_id") int id,
            @RequestHeader("Authorization") String bearer
    ) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            return attendanceService.getRecord(subjectCode, localDate, id, bearer);
        }catch (DateTimeParseException e){
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/record-history")
    public ResponseEntity<AttendanceRecordHistoryResponse> getRecordHistory(
            @RequestParam String subjectCode,
            @RequestHeader("Authorization") String bearer
    ) {
        try {
            return attendanceService.getHistoryRecord(subjectCode, bearer);
        }catch (DateTimeParseException e){
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/available-records")
    public ResponseEntity<AvailableRecords> getAvailableRecord(
            @RequestParam String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return attendanceService.getRecord(subjectCode, bearer);
    }
    @PostMapping("/clear")
    public ResponseEntity<Response> clearSubjectStudents(@RequestParam String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return build(lecturerService.clearSubjectStudents(subjectCode, bearer));
    }
    @PostMapping("/add")
    public ResponseEntity<Response> addStudentToSubject(
            @RequestParam String studentId,
            @RequestParam String subjectCode,
            @RequestHeader("Authorization") String bearer) {
        return lecturerService.addStudentToSubject(bearer, studentId, subjectCode);
    }
    private ResponseEntity<Response> build(ResponseEntity<String> response) {
        return new ResponseEntity<>(new Response(response.getBody()), response.getStatusCode());
    }
    @PostMapping("/suspend")
    public ResponseEntity<Response> suspendStudentFromMarkingAttendance(
            @RequestParam String subjectCode,
            @RequestParam String studentId,
            @RequestHeader("Authorization") String bearer,
            @RequestParam boolean suspend) {
        log.info("Received request to suspend/restore student: {}, subject: {}, suspend: {}", studentId, subjectCode, suspend);
        return lecturerService.suspendStudentFromMarkingAttendance(bearer, subjectCode, studentId,suspend);
    }
    @GetMapping("/student-record")
    private ResponseEntity<StudentAttendanceRecordResponse> viewAttendanceRecord(
            @RequestHeader("Authorization") String bearer,
            @RequestParam String studentId,
            @RequestParam String subjectCode) {
        return lecturerService.viewAttendanceRecord(bearer, studentId, subjectCode);
    }
    @GetMapping("/print")
    public ResponseEntity<ByteArrayResource> getRecordExcelSheet(@RequestParam String subjectCode,
            @RequestParam String date,
            @RequestParam("sort_id") int id,
            @RequestHeader("Authorization") String bearer) {
        LocalDate localDate = LocalDate.parse(date);
        return attendanceService.getAttendanceExcel(subjectCode, localDate, id, bearer);
    }

}