package com.backend.FaceRecognition.services.authorization_service.student_service;
import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.Attendance;
import com.backend.FaceRecognition.entities.EncodedImages;
import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.repository.AttendanceRepository;
import com.backend.FaceRecognition.repository.EncodedImagesRepository;
import com.backend.FaceRecognition.repository.StudentRepository;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.extras.ProfilePictureService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.utils.EncodedImage;
import com.backend.FaceRecognition.utils.FaceRecognitionEndpoints;
import com.backend.FaceRecognition.utils.StudentProfile;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class StudentService {
    private final EncodedImagesRepository encodedImagesRepository;
    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final FaceRecognitionEndpoints faceRecognitionEndpoints;
    private final JwtService jwtService;
    private final ApplicationUserService applicationUserService;
    private final AttendanceRepository attendanceRepository;
    private final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    private final ProfilePictureService profilePictureService;

    @Lazy
    public StudentService(EncodedImagesRepository encodedImagesRepository, StudentRepository studentRepository, FaceRecognitionEndpoints faceRecognitionEndpoints, JwtService jwtService, @Lazy ApplicationUserService applicationUserService, AttendanceRepository attendanceRepository, ProfilePictureService profilePictureService) {
        this.encodedImagesRepository = encodedImagesRepository;
        this.studentRepository = studentRepository;
        this.faceRecognitionEndpoints = faceRecognitionEndpoints;
        this.jwtService = jwtService;
        this.applicationUserService = applicationUserService;
        this.attendanceRepository = attendanceRepository;
        this.profilePictureService = profilePictureService;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    public Optional<Student> getStudentById(String matriculationNumber) {
        return studentRepository.findById(matriculationNumber);
    }
    public void saveStudent(Student student) {
        studentRepository.save(student);
    }
    public void saveAll(Collection<Student> student) {
         studentRepository.saveAll(student);
    }

    @Transactional
    public ResponseEntity<String> addStudentImage(MultipartFile file, String auth) {
        log.info("Received request to add image for student with authorization header");

        String token = jwtService.extractTokenFromHeader(auth);
        String studentId = jwtService.getId(token);
        log.info("Extracted student ID from token: {}", studentId);

        Optional<Student> studentOptional = getStudentById(studentId);
        if (studentOptional.isEmpty()) {
            log.warn("Student not found for ID: {}", studentId);
            return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
        }

        Student student = studentOptional.get();
        log.info("Adding image for student: {}", student.getMatriculationNumber());

        // Validate file type and size before proceeding
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            log.warn("File is empty or exceeds size limit for student ID: {}", studentId);
            return new ResponseEntity<>("Invalid file or file too large", HttpStatus.BAD_REQUEST);
        }

        String url = faceRecognitionEndpoints.getEndpoint("ip") + "?student_id=" + studentId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Assuming getResource() gives InputStreamResource
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            log.info("Sending image to face recognition service at URL: {}", url);
            ResponseEntity<EncodedImage> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    EncodedImage.class);

            EncodedImage image = responseEntity.getBody();
            if (image == null || image.getData() == null) {
                log.error("Invalid or empty response received from face recognition service");
                return new ResponseEntity<>("Bad Image: Could not encode image", HttpStatus.BAD_REQUEST);
            }

            if ("Invalid Amount of Faces detected".equals(image.getMessage())) {
                log.warn("Face recognition service detected an invalid number of faces for student ID: {}", studentId);
                return new ResponseEntity<>("Invalid amount of faces detected", HttpStatus.BAD_REQUEST);
            }

            EncodedImages imageEntity = EncodedImages.builder()
                    .data(image.getData())
                    .matriculationNumber(studentId)
                    .build();
            encodedImagesRepository.save(imageEntity);

            log.info("Image successfully saved for student ID: {}", studentId);
            return new ResponseEntity<>("Saved successfully", HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            log.error("HTTP error response from face recognition service: Status = {}, Message = {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Unexpected error occurred while adding student image for student ID: {}", studentId, e);
            return new ResponseEntity<>("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public Set<Student> getAllStudentsOfferingCourse(String subjectCode) {
        return studentRepository.findAllStudentsBySubjectCode(subjectCode);
    }

    public ArrayList<Student> getAllStudentsOfferingCourse2(String subjectCode) {
        return studentRepository.findAllStudentsBySubjectCodeArrayList(subjectCode);
    }


    public ResponseEntity<StudentProfile> getMyProfile(String studentId) {
        log.info("Fetching profile for student ID: {}", studentId);

        // Retrieve ApplicationUser by student ID
        ApplicationUser applicationUser = applicationUserService.findUser(studentId).orElse(null);
        if (applicationUser == null) {
            log.warn("Application user not found for ID: {}", studentId);
            return new ResponseEntity<>(StudentProfile.builder().message("Student not found").build(), HttpStatus.NOT_FOUND);
        }

        // Retrieve Student entity by ApplicationUser ID
        Student student = studentRepository.findById(applicationUser.getId()).orElse(null);
        if (student == null) {
            log.warn("Student entity not found for ID: {}", studentId);
            return new ResponseEntity<>(StudentProfile.builder().message("Student not found").build(), HttpStatus.NOT_FOUND);
        }

        log.info("Student found: {}", student);

        // Process courses
        StudentProfile.Course[] courses = student.getSubjects().stream()
                .map(subject -> new StudentProfile.Course(subject.getSubjectCode(), subject.getSubjectTitle()))
                .toArray(StudentProfile.Course[]::new);

        // Calculate attendance score
        List<Attendance> studentAttendance = attendanceRepository.findByStudentId(studentId);
        long presentCount = studentAttendance.stream()
                .filter(v -> v.getStatus() == AttendanceStatus.PRESENT)
                .count();

        String attendanceScore = studentAttendance.isEmpty()
                ? "Nil"
                : String.format("%.2f %%", (presentCount * 100.0 / studentAttendance.size()));

        log.info("Calculated attendance score: {}", attendanceScore);

        // Retrieve profile picture
        byte[] imageData = profilePictureService.getProfilePicture(studentId).getBody();
        if (imageData == null) {
            log.warn("Profile picture not found for student ID: {}", studentId);
        } else {
            log.info("Successfully retrieved profile picture for student ID: {}", studentId);
        }

        // Build student profile response
        StudentProfile studentProfile = StudentProfile.builder()
                .message("Successfully Fetched Student Profile")
                .data(
                        StudentProfile.StudentData.builder()
                                .name(String.format("%s %s %s", student.getLastname(), student.getFirstname(), student.getMiddleName()))
                                .matriculationNumber(student.getMatriculationNumber())
                                .email(student.getSchoolEmail())
                                .profilePicture(imageData)
                                .phoneNumber(applicationUser.getPhoneNumber())
                                .address(applicationUser.getAddress())
                                .attendanceCount(String.valueOf(presentCount))
                                .totalPossible(String.valueOf(studentAttendance.size()))
                                .attendanceScore(attendanceScore)
                                .courses(courses)
                                .build()
                ).build();

        log.info("Successfully fetched student profile for ID: {}", studentId);
        return ResponseEntity.ok(studentProfile);
    }


}
