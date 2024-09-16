package com.backend.FaceRecognition.services.authorization_service.admin;

import com.backend.FaceRecognition.constants.Role;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.repository.SuspensionRepository;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.GetListOfUsers;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;
import com.backend.FaceRecognition.utils.subject.AllSubjects;
import com.backend.FaceRecognition.utils.subject.SubjectRequest;
import com.backend.FaceRecognition.utils.subject.SubjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {
    private final ApplicationUserService applicationUserService;
    private final SubjectService subjectService;
    private final StudentService studentService;
    private final JwtService jwtService;
    private final SuspensionRepository suspensionRepository;

    public AdminService(ApplicationUserService applicationUserService, SubjectService subjectService,
                        StudentService studentService, JwtService jwtService, SuspensionRepository suspensionRepository) {
        this.applicationUserService = applicationUserService;
        this.subjectService = subjectService;
        this.studentService = studentService;
        this.jwtService = jwtService;

        this.suspensionRepository = suspensionRepository;
    }
    public ResponseEntity<String> lockAccount(String id, String bearer) {
        log.info("Request to lock account with ID: {} and bearer token: {}", id, bearer);

        try {
            ResponseEntity<String> response = changeAccountStatus(id, bearer, true);
            log.info("Account locked successfully for ID: {}", id);
            return response;
        } catch (Exception e) {
            log.error("Error locking account with ID: {}", id, e);
            return ResponseEntity.status(500).body("An error occurred while locking the account");
        }
    }
    public ResponseEntity<String> unlockAccount(String id, String bearer) {
        log.info("Request to unlock account with ID: {} and bearer token: {}", id, bearer);

        try {
            ResponseEntity<String> response = changeAccountStatus(id, bearer, false);
            log.info("Account unlocked successfully for ID: {}", id);
            return response;
        } catch (Exception e) {
            log.error("Error unlocking account with ID: {}", id, e);
            return ResponseEntity.status(500).body("An error occurred while unlocking the account");
        }
    }

    public ResponseEntity<String> changeAccountStatus(String id, String bearer, boolean lock) {
        log.info("Changing account status for ID: {}. Lock: {}", id, lock);
        // Extract JWT token and get user ID
        String jwtToken = jwtService.extractTokenFromHeader(bearer);
        String userId = jwtService.getId(jwtToken);
        log.debug("Extracted JWT token: {}", jwtToken);
        log.debug("User ID extracted from token: {}", userId);
        // Retrieve requesting user and their roles
        Optional<ApplicationUser> requestingUserOptional = applicationUserService.findUser(userId);
        if (requestingUserOptional.isEmpty()) {
            log.warn("Requesting user with ID: {} not found", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: requesting user not found.");
        }
        ApplicationUser requestingUser = requestingUserOptional.get();
        Set<Role> roles = requestingUser.getUserRole();
        log.info("Requesting user roles: {}", roles);

        // Check if the user has the required roles to perform the operation
        Optional<ApplicationUser> userOptional = applicationUserService.findUser(id);
        if (userOptional.isEmpty()) {
            log.warn("User with ID: {} not found", id);
            return ResponseEntity.notFound().build();
        }

        ApplicationUser user = userOptional.get();
        if (roles.contains(Role.ROLE_SUPER_ADMIN)) {
            log.info("Super admin role detected, allowing status change for ID: {}", id);
        } else if (user.hasRole(Role.ROLE_SUPER_ADMIN) || user.hasRole(Role.ROLE_ADMIN)) {
            log.warn("Unauthorized attempt by user with ID: {} to change account status for ID: {}", userId, id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to change account status.");
        } else {
            log.info("Changing status of user with ID: {} to {}", id, lock ? "unlocked" : "locked");
            user.setEnabled(!lock); // false to lock, true to unlock
            applicationUserService.update(user);
            return ResponseEntity.ok(lock ? "Account locked successfully." : "Account unlocked successfully.");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized to change account status.");
    }


    public ResponseEntity<String> deleteSubject(String request) {
        log.info("Request to delete subject with code: {}", request);
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(request);
        if (subjectOptional.isEmpty()) {
            log.warn("Subject with code: {} does not exist", request);
            return new ResponseEntity<>("Subject does not exist", HttpStatus.CONFLICT);
        }

        log.info("Subject with code: {} found, proceeding with deletion", request);
        Set<Student> students = studentService.getAllStudentsOfferingCourse(request);

        if (students.isEmpty()) {
            log.info("No students are currently offering the subject with code: {}", request);
        } else {
            log.info("Updating students enrolled in subject with code: {}", request);
            students.forEach(student -> {
                Set<Subject> subjects = student.getSubjects();
                // Remove the subject from the set
                subjects.removeIf(subject -> subject.getSubjectCode().equals(request));
                log.debug("Updated student ID: {} with new subjects list", student.getMatriculationNumber());
            });
            studentService.saveAll(students);
            log.info("Updated students' records after subject deletion");
        }

        subjectService.deleteSubjectByCode(request);
        log.info("Subject with code: {} successfully deleted", request);

        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    public ResponseEntity<AllSubjects> getAllSubject(boolean student) {
        log.info("Request to get all subjects with student data: {}", student);
        List<Subject> subjects = subjectService.findAll();
        log.info("Retrieved {} subjects from the service", subjects.size());
        if (student) {
            return ResponseEntity.ok(new
                    AllSubjects(subjects.stream().map(this::parse).collect(Collectors.toList())));
        }
        List<SubjectResponse> myList = subjects.stream()
                .filter(Objects::nonNull)
                .map(s -> SubjectResponse
                        .builder()
                        .subjectTitle(s.getSubjectTitle())
                        .idLecturerInCharge(s.getLecturerInCharge() == null ? "" : s.getLecturerInCharge().getId())
                        .subjectCode(s.getSubjectCode())
                        .build())
                .toList();
        AllSubjects allSubjectsNoStudentData = new AllSubjects(myList);
        return ResponseEntity.ok(allSubjectsNoStudentData);
    }

    public ResponseEntity<SubjectResponse> getSubject(String subjectCode) {
        Optional<Subject> optionalSubject = subjectService.findSubjectByCode(subjectCode);
        log.info("getting subject");
        if (optionalSubject.isEmpty()) {
            return new ResponseEntity<>(new SubjectResponse("Subject not found"),
                    HttpStatus.NOT_FOUND);
        }
        SubjectResponse response = parse(optionalSubject.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public SubjectResponse parse(Subject subject) {
        SubjectResponse response = new SubjectResponse();
        response.setSubjectCode(subject.getSubjectCode());
        response.setSubjectTitle(subject.getSubjectTitle());
        response.setIdLecturerInCharge(
                subject.getLecturerInCharge() == null ? "" : subject.getLecturerInCharge().getId());
        Set<Student> students = studentService
                .getAllStudentsOfferingCourse(subject.getSubjectCode());
        List<SubjectResponse.Metadata> matriculationNum = students.
                stream()
                .map(v -> SubjectResponse.Metadata.builder()
                        .studentId(v.getMatriculationNumber())
                        .firstname(v.getFirstname())
                        .lastname(v.getLastname())
                        .isSuspended(suspensionRepository.findByStudentIdAndSubjectId(v.getMatriculationNumber(),subject.getSubjectCode()).isPresent())
                        .build()
                ).collect(Collectors.toList());
        response.setStudents(matriculationNum);
        response.setMessage("Fetched Successfully");
        if (subject.getLecturerInCharge() != null) {
            response.setIdLecturerInCharge(subject.getLecturerInCharge().getId());
        }
        return response;
    }

    public ResponseEntity<String> clearAllStudentSubjects() {
        List<Student> students = studentService.getAllStudents();
        students.forEach(Student::clear);
        studentService.saveAll(students);
        return new ResponseEntity<>("Cleared successfully", HttpStatus.OK);
    }

    public ResponseEntity<String> updateSubject(SubjectRequest request) {
        Optional<Subject> subject1 = subjectService.findSubjectByCode(request.getSubjectCode());
        if (subject1.isEmpty()) {
            return new ResponseEntity<>("Subject does not exist", HttpStatus.CONFLICT);
        }
        ApplicationUser user = subject1.get().getLecturerInCharge();
        if (request.getIdLecturerInCharge() != null
                && !request.getIdLecturerInCharge().isEmpty()) {
            user = applicationUserService
                    .findUser(request.getIdLecturerInCharge())
                    .orElse(null);
            if (user == null || !user.hasRole(Role.ROLE_LECTURER)) {
                return new ResponseEntity<>("Not A Lecturer", HttpStatus.BAD_REQUEST);
            }
        }
        Subject subject = subject1.get();
        subject.setSubjectCode(request.getSubjectCode());
        String title = request.getSubjectTitle() == null ? subject.getSubjectTitle() : request.getSubjectTitle();
        subject.setSubjectTitle(title);
        subject.setLecturerInCharge(user);
        subjectService.save(subject);
        return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
    }

    public Subject parse(SubjectRequest request) {
        ApplicationUser user = null;
        if (request.getIdLecturerInCharge() != null) {
            user = applicationUserService.findUser(request.getIdLecturerInCharge())
                    .orElse(null);
            if (user == null || !user.hasRole(Role.ROLE_LECTURER)) {
                return null;
            }
        }
        Subject subject = new Subject();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectTitle(request.getSubjectTitle());
        subject.setLecturerInCharge(user);
        return subject;
    }

    public ResponseEntity<String> addSubject(SubjectRequest request) {
        log.info("Request to add subject with code: {}", request.getSubjectCode());

        Optional<Subject> existingSubject = subjectService.findSubjectByCode(request.getSubjectCode());
        if (existingSubject.isPresent()) {
            log.warn("Subject with code: {} already exists", request.getSubjectCode());
            return new ResponseEntity<>("Subject already added", HttpStatus.CONFLICT);
        }

        Subject subject = parse(request);
        if (subject == null) {
            log.warn("Failed to parse subject request: {}", request);
            return new ResponseEntity<>("Invalid subject data", HttpStatus.BAD_REQUEST);
        }

        subjectService.save(subject);
        log.info("Subject with code: {} successfully added", request.getSubjectCode());
        return new ResponseEntity<>("Saved successfully", HttpStatus.OK);
    }

    public ResponseEntity<ApplicationUser> getUser(String userId, String bearer) {
        log.info("Request to get user with ID: {}", userId);

        Optional<ApplicationUser> applicationUserOptional = applicationUserService.findUser(userId);
        String tokenId = jwtService.getId(jwtService.extractTokenFromHeader(bearer));
        Optional<ApplicationUser> requestingUserOptional = applicationUserService.findUser(tokenId);

        if (requestingUserOptional.isEmpty()) {
            log.warn("Requesting user with ID: {} not found", tokenId);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ApplicationUser requestingUser = requestingUserOptional.get();
        if (applicationUserOptional.isPresent()) {
            ApplicationUser applicationUser = applicationUserOptional.get();
            if (applicationUser.getUserRole().contains(Role.ROLE_SUPER_ADMIN) &&
                    !requestingUser.getUserRole().contains(Role.ROLE_SUPER_ADMIN)) {
                log.warn("Unauthorized access attempt by user with ID: {}", tokenId);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            log.info("User with ID: {} found and accessible", userId);
            return ResponseEntity.ok(applicationUser);
        }

        log.warn("User with ID: {} not found", userId);
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<GetListOfUsers> getAllStudents() {
        log.info("Request to get all students");

        List<ApplicationUser> users = applicationUserService.findAllUsers();
        log.info("Retrieved {} users from the service", users.size());

        List<ApplicationUserRequest> studentRequests = users.stream()
                .filter(user -> user.hasRole(Role.ROLE_STUDENT))
                .map(user -> ApplicationUserRequest.builder()
                        .id(user.getId())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .middleName(user.getMiddleName())
                        .phoneNumber(user.getPhoneNumber())
                        .accountStatus(user.isEnabled() ? "ACTIVE" : "INACTIVE")
                        .schoolEmail(user.getSchoolEmail())
                        .build())
                .collect(Collectors.toList());

        log.info("Filtered {} students from the retrieved users", studentRequests.size());
        return ResponseEntity.ok(new GetListOfUsers(studentRequests));
    }


    public ResponseEntity<GetListOfUsers> getAll(String lowerCase, String bearer) {
        log.info("Request to get all users of type: {}", lowerCase);

        return switch (lowerCase.toLowerCase()) {
            case "student" -> {
                log.info("Retrieving all students");
                ResponseEntity<GetListOfUsers> response = getAllStudents();
                if (response.getBody() != null)  log.info("Retrieved {} students", (response.getBody()).getData().size());
                yield response;
            }
            case "instructor" -> {
                log.info("Retrieving all instructors");
                List<ApplicationUser> users = applicationUserService.findAllUsers().stream()
                        .filter(user -> user.hasRole(Role.ROLE_LECTURER))
                        .collect(Collectors.toList());
                List<ApplicationUserRequest> userRequests = users.stream()
                        .map(v -> ApplicationUserRequest.builder()
                                .id(v.getId())
                                .firstname(v.getFirstname())
                                .lastname(v.getLastname())
                                .accountStatus(v.isEnabled() ? "ACTIVE" : "INACTIVE")
                                .phoneNumber(v.getPhoneNumber())
                                .middleName(v.getMiddleName())
                                .schoolEmail(v.getSchoolEmail())
                                .build())
                        .collect(Collectors.toList());
                log.info("Retrieved {} instructors", userRequests.size());
                yield ResponseEntity.ok(new GetListOfUsers(userRequests));
            }
            case "admin" -> {
                String tokenId = jwtService.getId(jwtService.extractTokenFromHeader(bearer));
                log.info("Requesting user ID: {}", tokenId);

                var reqUser = applicationUserService.findUser(tokenId).orElse(null);
                if (reqUser == null || !reqUser.getUserRole().contains(Role.ROLE_SUPER_ADMIN)) {
                    log.warn("Unauthorized access attempt by user with ID: {}", tokenId);
                    yield new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }

                log.info("User with ID: {} is authorized as SUPER_ADMIN", tokenId);

                List<ApplicationUser> users = applicationUserService.findAllUsers().stream()
                        .filter(user -> user.hasRole(Role.ROLE_ADMIN))
                        .collect(Collectors.toList());
                List<ApplicationUserRequest> userRequests = users.stream()
                        .map(v -> ApplicationUserRequest.builder()
                                .id(v.getId())
                                .lastname(v.getLastname())
                                .firstname(v.getFirstname())
                                .middleName(v.getMiddleName())
                                .phoneNumber(v.getPhoneNumber())
                                .accountStatus(v.isEnabled() ? "ACTIVE" : "INACTIVE")
                                .schoolEmail(v.getSchoolEmail())
                                .build())
                        .collect(Collectors.toList());
                log.info("Retrieved {} admins", userRequests.size());
                yield ResponseEntity.ok(new GetListOfUsers(userRequests));
            }
            default -> {
                log.warn("Invalid user type requested: {}", lowerCase);
                yield ResponseEntity.badRequest().build();
            }
        };
    }


}
