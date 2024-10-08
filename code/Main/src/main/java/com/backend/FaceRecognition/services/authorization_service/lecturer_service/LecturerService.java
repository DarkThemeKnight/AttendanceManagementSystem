package com.backend.FaceRecognition.services.authorization_service.lecturer_service;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.*;
import com.backend.FaceRecognition.repository.AttendanceSetupPolicyRepository;
import com.backend.FaceRecognition.repository.SuspensionRepository;
import com.backend.FaceRecognition.services.attendance_service.AttendanceService;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.ListOfSubjects;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.StudentAttendanceRecordResponse;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LecturerService {
    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final SuspensionRepository suspensionRepository;
    private final JwtService jwtService;
    private final ApplicationUserService applicationUserService;

    public LecturerService(AttendanceService attendanceService, StudentService studentService, SubjectService subjectService, SuspensionRepository suspensionRepository, JwtService jwtService,@Lazy ApplicationUserService applicationUserService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.suspensionRepository = suspensionRepository;
        this.jwtService = jwtService;
        this.applicationUserService = applicationUserService;
    }
    @Lazy
    @Autowired
    private AttendanceSetupPolicyRepository attendanceSetupPolicyRepository;
    public ResponseEntity<SubjectResponse> getSubject(String subjectCode,String bearer) {
        log.info("Received request to get subject details for subjectCode: {}", subjectCode);

        // Attempt to retrieve the subject
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            log.warn("Subject not found for subjectCode: {}", subjectCode);
            return new ResponseEntity<>(new SubjectResponse("Subject not found"), HttpStatus.NOT_FOUND);
        }

        // Check if the operation can be performed based on the authorization
        boolean canPerformOperation = !cantPerformOperation(bearer, optionalSubject.get());
        if (canPerformOperation) {
            log.info("Authorization successful. Preparing response for subjectCode: {}", subjectCode);
            SubjectResponse response = parse(optionalSubject.get());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            log.warn("Unauthorized access attempt for subjectCode: {} with bearer token: {}", subjectCode, bearer);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    private boolean cantPerformOperation(String authorizationHeader, Subject subject){

        // Extract JWT token
        String jwtToken = jwtService.extractTokenFromHeader(authorizationHeader);
        log.debug("Extracted JWT token from authorization header: {}", jwtToken); // Consider removing or obfuscating sensitive parts

        // Extract lecturer ID from token
        String lecturerId = jwtService.getId(jwtToken);
        log.debug("Extracted lecturer ID from JWT token: {}", lecturerId);

        // Check if the lecturer ID matches the subject's lecturer in charge
        boolean cannotPerform = !subject.getLecturerInCharge().getId().equals(lecturerId);
        if (cannotPerform) {
            log.warn("Operation cannot be performed. Lecturer ID {} does not match the lecturer in charge {} for subject {}",
                    lecturerId, subject.getLecturerInCharge().getId(), subject.getSubjectCode());
        } else {
            log.info("Operation authorized for Lecturer ID {} on subject {}", lecturerId, subject.getSubjectCode());
        }

        return cannotPerform;
    }

    public SubjectResponse parse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setSubjectCode(subject.getSubjectCode());
        response.setSubjectTitle(subject.getSubjectTitle());
        response.setIdLecturerInCharge(
                subject.getLecturerInCharge() == null ? "" : subject.getLecturerInCharge().getId());
        Set<Student> students = studentService
                .getAllStudentsOfferingCourse(subject.getSubjectCode());
        List<SubjectResponse.Metadata> matriculationNum = new ArrayList<>(students.
                stream()
                .map(v -> {
                            ResponseEntity<List<Attendance>> response1 =
                                    attendanceService.getStudentRecord(v.getMatriculationNumber());
                            AtomicInteger score = new AtomicInteger();
                            String percentage = "0";
                            if (response1.getStatusCode().is2xxSuccessful() && response1.getBody() != null) {
                                response1.getBody().stream().filter(attendance -> attendance.getSubjectId().equalsIgnoreCase(subject.getSubjectCode())).forEach(attendance -> {
                                    if (attendance.getStatus().equals(AttendanceStatus.PRESENT)) {
                                        score.getAndIncrement();
                                    }
                                });
                                percentage = String.format("%.2f", (score.get() * 100.0) / response1.getBody().size());
                            }
                            SubjectResponse.Metadata subjectResponseMetadata = SubjectResponse.Metadata.builder()
                                    .studentId(v.getMatriculationNumber())
                                    .firstname(v.getFirstname())
                                    .lastname(v.getLastname())
                                    .percentage(percentage)
                                    .build();
                            Optional<Suspension> suspension = suspensionRepository.findByStudentIdAndSubjectId(v.getMatriculationNumber(), subject.getSubjectCode());
                            subjectResponseMetadata.setSuspended(suspension.isPresent());
                            return subjectResponseMetadata;
                        }
                ).toList());
        matriculationNum.sort(Comparator.comparing(v-> Double.parseDouble(v.getPercentage())));
        response.setStudents(matriculationNum);
        response.setMessage("Fetched Successfully");
        if (subject.getLecturerInCharge() != null) {
            response.setIdLecturerInCharge(subject.getLecturerInCharge().getId());
        }
        return response;
    }


    public ResponseEntity<String> clearSubjectStudents(String subjectCode,String auth) {
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>("subject Not found", HttpStatus.NOT_FOUND);
        }
        Subject subject = optionalSubject.get();
        if (cantPerformOperation(auth, subject)){
            return new ResponseEntity<>("Unauthorized",HttpStatus.UNAUTHORIZED);
        }
        Set<Student> student = studentService.getAllStudentsOfferingCourse(subject.getSubjectCode());
        student.forEach(student1 ->  {
            student1.remove(subject);
            studentService.saveStudent(student1);
        });
        return new ResponseEntity<>("Cleared", HttpStatus.OK);
    }



    public ResponseEntity<Response> suspendStudentFromMarkingAttendance(String auth, String subjectCode, String studentId, boolean suspend) {
        log.debug("Validating subject with code: {}", subjectCode);
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);

        if (optionalSubject.isEmpty()) {
            log.warn("Subject with code: {} not found", subjectCode);
            return new ResponseEntity<>(new Response("Subject Not found"), HttpStatus.NOT_FOUND);
        }

        if (cantPerformOperation(auth, optionalSubject.get())) {
            log.warn("Unauthorized attempt to modify suspension for subject: {}", subjectCode);
            return new ResponseEntity<>(new Response("Unauthorized"), HttpStatus.UNAUTHORIZED);
        }

        if (suspend) {
            log.debug("Checking if student: {} is already suspended for subject: {}", studentId, subjectCode);
            if (suspensionRepository.findByStudentIdAndSubjectId(studentId, subjectCode).isPresent()) {
                log.info("Student: {} already suspended for subject: {}", studentId, subjectCode);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response("Already suspended"));
            }
            Suspension suspension = new Suspension(null, studentId, subjectCode);
            suspensionRepository.save(suspension);
            log.info("Student: {} suspended successfully for subject: {}", studentId, subjectCode);
            return ResponseEntity.ok(new Response("Suspended successfully"));
        } else {
            log.debug("Checking if student: {} is currently suspended for subject: {}", studentId, subjectCode);
            var optional = suspensionRepository.findByStudentIdAndSubjectId(studentId, subjectCode);
            if (optional.isPresent()) {
                suspensionRepository.delete(optional.get());
                log.info("Suspension for student: {} in subject: {} has been restored", studentId, subjectCode);
                return ResponseEntity.status(HttpStatus.OK).body(new Response("Restored"));
            } else {
                log.info("Student: {} is not suspended for subject: {}", studentId, subjectCode);
                return new ResponseEntity<>(new Response("Is not a Suspended Student"),HttpStatus.CONFLICT);
            }
        }
    }

    public ResponseEntity<StudentAttendanceRecordResponse> viewAttendanceRecord(String auth, String studentId, String subjectCode) {
        log.info("Attempting to view attendance record for student: {}, subject: {}", studentId, subjectCode);

        // Step 1: Fetch subject and perform authorization check
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        if (optionalSubject.isEmpty()) {
            log.warn("Subject with code {} not found", subjectCode);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Subject subject = optionalSubject.get();

        if (cantPerformOperation(auth, subject)) {
            log.warn("Unauthorized access attempt by user to view attendance for subject: {}", subjectCode);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Step 2: Fetch student attendance records
        ResponseEntity<List<Attendance>> response = attendanceService.getStudentRecord(studentId);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Attendance records for student {} not found", studentId);
            return ResponseEntity.notFound().build();
        }

        List<Attendance> attendanceList = response.getBody();
        if (attendanceList == null || attendanceList.isEmpty()) {
            log.error("Attendance list is empty or null for student: {}", studentId);
            return ResponseEntity.internalServerError().build();
        }

        // Step 3: Filter attendance for the specific subject and map to response
        List<StudentAttendanceRecordResponse.DefaultResponse> getDefault = attendanceList.stream()
                .filter(Objects::nonNull)
                .filter(attendance -> attendance.getSubjectId().equalsIgnoreCase(subjectCode))
                .map(attendance -> {
                    Subject subjectDetail = subjectService.findSubjectByCode(attendance.getSubjectId()).orElse(null);
                    return subjectDetail != null
                            ? new StudentAttendanceRecordResponse.DefaultResponse(
                            subjectDetail.getSubjectCode(),
                            subjectDetail.getSubjectTitle(),
                            attendance.getDate(),
                            attendance.getStatus())
                            : null;
                })
                .filter(Objects::nonNull)  // Ensure null values aren't passed
                .toList();

        if (getDefault.isEmpty()) {
            log.warn("No attendance records found for student: {} in subject: {}", studentId, subjectCode);
            return ResponseEntity.noContent().build();
        }

        // Step 4: Build and return the response
        log.info("Successfully retrieved attendance records for student: {} in subject: {}", studentId, subjectCode);
        return ResponseEntity.ok(new StudentAttendanceRecordResponse(attendanceList.get(0).getStudentId(), getDefault));
    }

    @Transactional
    public ResponseEntity<Response> addStudentToSubject(String auth, String studentId, String subjectCode) {
        log.info("Attempting to add student: {} to subject: {}", studentId, subjectCode);

        // Step 1: Fetch subject and perform authorization check
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            log.warn("Subject with code {} not found", subjectCode);
            return new ResponseEntity<>(new Response("Subject not found"), HttpStatus.NOT_FOUND);
        }

        Subject subject = subjectOptional.get();

        if (cantPerformOperation(auth, subject)) {
            log.warn("Unauthorized access attempt to add student to subject: {}", subjectCode);
            return new ResponseEntity<>(new Response("Unauthorized to add student of id => "+studentId+" to subject "+subjectCode+" NOT Lecturer of this course"), HttpStatus.UNAUTHORIZED);
        }

        // Step 2: Fetch student details
        Student student = studentService.getStudentById(studentId).orElse(null);
        if (student == null) {
            log.warn("Student with ID {} not found", studentId);
            return new ResponseEntity<>(new Response("Student not found"), HttpStatus.NOT_FOUND);
        }

        // Step 3: Add student to the subject
        try {
            log.info("Adding student: {} to subject: {}", studentId, subjectCode);
            student.add(subject);  // Assuming this method adds the subject to the student
            studentService.saveStudent(student);  // Save the updated student
            subjectService.save(subject);  // Save the updated subject
        } catch (Exception e) {
            log.error("Failed to add student: {} to subject: {}", studentId, subjectCode, e);
            return new ResponseEntity<>(new Response("Failed to add student to subject"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Step 4: Return success response
        log.info("Successfully added student: {} to subject: {}", studentId, subjectCode);
        return new ResponseEntity<>(new Response("Student added successfully"), HttpStatus.OK);
    }


    public ResponseEntity<ListOfSubjects> getSubjectList(String auth) {
        log.debug("Extracting token from authorization header");
        String token = jwtService.extractTokenFromHeader(auth);

        log.debug("Extracting user ID from token");
        String id = jwtService.getId(token);

        log.debug("Finding user by ID: {}", id);
        var app = applicationUserService.findUser(id).orElse(null);
        if (app == null) {
            log.warn("User not found with ID: {}", id);
            // You may want to return a suitable error response here
        }

        log.debug("Finding all subjects for lecturer: {}", app);
        Set<Subject> subjects = subjectService.findAllByLecuturerInCharge(app);

        log.debug("Building list of subjects for response");
        ListOfSubjects listOfSubjects = ListOfSubjects.builder()
                .lecturerID(id)
                .lecturerName(app.getLastname() + " " + app.getFirstname())
                .data(subjects.stream().map(subject -> ListOfSubjects.MetaData.builder()
                        .subjectId(subject.getSubjectCode())
                        .subjectTitle(subject.getSubjectTitle())
                        .build()).toList())
                .build();

        log.info("Returning list of subjects for lecturer ID: {}", id);
        return ResponseEntity.ok(listOfSubjects);
    }

    @Transactional
    public ResponseEntity<Response> addStudentToSubject(String bearer, MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return ResponseEntity.badRequest().body(new Response("Filename is null"));
        }
        if (!filename.endsWith(".csv")) {
            return ResponseEntity.badRequest().body(new Response("Filename is not a CSV file"));
        }
        List<String> validationErrors = new ArrayList<>();
        List<String> successfulAdds = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length != 2) { // Expecting [studentId, subjectCode]
                    validationErrors.add("Invalid number of tokens in line: " + line);
                    continue; // Skip this line if not enough tokens
                }
                String studentId = tokens[0];
                String subjectCode = tokens[1];
                // Attempt to add the student to the subject
                ResponseEntity<Response> response = addStudentToSubject(bearer, studentId, subjectCode);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    validationErrors.add("Failed to add student: " + studentId + " to subject: " + subjectCode + ". Reason: " + response.getBody().getMessage());
                } else {
                    successfulAdds.add("Successfully added student: " + studentId + " to subject: " + subjectCode);
                }
            }
        } catch (IOException ex) {
            log.error("Error processing file", ex);
            return ResponseEntity.badRequest().body(new Response("Error Processing file: " + ex.getMessage()));
        }catch (Exception ex) {
            return ResponseEntity.badRequest().body(new Response("Error Processing file: " + ex.getMessage()));
        }
        StringBuilder responseMessage = new StringBuilder();
        if (!successfulAdds.isEmpty()) {
            responseMessage.append(String.join("\n", successfulAdds)).append("\n");
        }
        if (!validationErrors.isEmpty()) {
            responseMessage.append("Errors occurred:\n").append(String.join("\n", validationErrors));
        }
        return ResponseEntity.ok(new Response(responseMessage.toString()));
    }


}
