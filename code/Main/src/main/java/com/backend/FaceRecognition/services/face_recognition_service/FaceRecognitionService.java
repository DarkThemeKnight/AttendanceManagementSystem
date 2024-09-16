package com.backend.FaceRecognition.services.face_recognition_service;

import com.backend.FaceRecognition.entities.Student;
import com.backend.FaceRecognition.entities.Subject;
import com.backend.FaceRecognition.services.image_request_service.EncodingService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.*;

import com.backend.FaceRecognition.utils.student.StudentRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
public class FaceRecognitionService {
    private final StudentService studentService;
    private final SubjectService subjectService;
    private final EncodingService encodingService;
    private final FaceRecognitionEndpoints faceRecognitionEndpoints;
    public FaceRecognitionService(StudentService studentService, SubjectService subjectService,
                                  EncodingService encodingService, FaceRecognitionEndpoints faceRecognitionEndpoints) {
        this.studentService = studentService;
        this.subjectService = subjectService;
        this.encodingService = encodingService;
        this.faceRecognitionEndpoints = faceRecognitionEndpoints;
    }


    public ResponseEntity<EncodeImageListResponse> getEncodings(String subjectCode) {
        log.info("Fetching encodings for subject code: {}", subjectCode);
        // Fetch the subject
        Subject subject = subjectService.findSubjectByCode(subjectCode).orElse(null);
        if (subject == null) {
            log.warn("Subject not found for subject code: {}", subjectCode);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Subject found: {}", subject);
        // Fetch students offering the course
        Set<Student> students = studentService.getAllStudentsOfferingCourse(subjectCode);
        if (students.isEmpty()) {
            log.info("No students found offering the course with subject code: {}", subjectCode);
        } else {
            log.info("Found {} students offering the course with subject code: {}", students.size(), subjectCode);
        }
        // Extract matriculation numbers
        List<String> matriculationNumbers = students.stream()
                .map(Student::getMatriculationNumber)
                .toList();
        log.info("Matriculation numbers extracted: {}", matriculationNumbers);
        // Get the response
        EncodeImageListResponse response = getResponse(matriculationNumbers);
        log.info("Encodings response prepared for subject code: {}", subjectCode);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private EncodeImageListResponse getResponse(List<String> matriculationNumbers) {
        List<EncodedImageResponse> imageResponses = matriculationNumbers.stream()
                .map(encodingService::getStudentEncodings)
                .filter(listResponseEntity -> listResponseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK))
                .map(ResponseEntity::getBody)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
        EncodeImageListResponse request = new EncodeImageListResponse();
        imageResponses.forEach(eI -> request.add(eI.getMatriculationNumber(), eI.getData()));
        return request;
    }


    public ResponseEntity<Student> recognizeFace( MultipartFile file, String subjectId) {
        log.info("Starting face recognition for subject ID: {}", subjectId);

        String endpoint = faceRecognitionEndpoints.getEndpoint("rec") + "?subject_id=" + subjectId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            log.info("Sending request to face recognition endpoint: {}", endpoint);

            ResponseEntity<StudentRequest> responseEntity = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    StudentRequest.class
            );

            log.info("Received response with status code: {}", responseEntity.getStatusCode());

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                StudentRequest val = responseEntity.getBody();
                if (val == null) {
                    log.warn("No student found in response for subject ID: {}", subjectId);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                log.info("Student ID found in response: {}", val.getStudent_id());
                Student student = studentService.getStudentById(val.getStudent_id()).orElse(null);

                if (student == null) {
                    log.warn("Student not found in the database for student ID: {}", val.getStudent_id());
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                log.info("Student successfully recognized and retrieved: {}", student);
                return new ResponseEntity<>(student, HttpStatus.OK);
            } else {
                log.error("Face recognition service returned error status: {}", responseEntity.getStatusCode());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error("Exception occurred during face recognition: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}