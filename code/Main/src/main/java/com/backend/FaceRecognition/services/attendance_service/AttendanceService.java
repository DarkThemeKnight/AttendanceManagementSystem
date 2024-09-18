package com.backend.FaceRecognition.services.attendance_service;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.*;
import com.backend.FaceRecognition.repository.AttendanceRepository;
import com.backend.FaceRecognition.repository.AttendanceSetupPolicyRepository;
import com.backend.FaceRecognition.repository.SuspensionRepository;
import com.backend.FaceRecognition.services.face_recognition_service.FaceRecognitionService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.services.authorization_service.student_service.StudentService;
import com.backend.FaceRecognition.services.subject.SubjectService;
import com.backend.FaceRecognition.utils.*;
import com.backend.FaceRecognition.utils.history.AttendanceRecordHistoryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceSetupPolicyRepository attendanceSetupRepository;
    private final AttendanceRepository attendanceRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final SubjectService subjectService;
    private final JwtService jwtService;
    private final StudentService studentService;
    private final SuspensionRepository suspensionRepository;
    private final ObjectMapper objectMapper;

    public ResponseEntity<InitializeAttendanceResponse> initializeAttendance(String subjectCode, String authorization, int duration) {
        log.info("Ïnitializing Attendance code => {}, duration => {}",subjectCode,duration);
        Optional<AttendanceSetupPolicy> setupPolicy = attendanceSetupRepository.findBySubjectIdAndAttendanceDate(subjectCode,LocalDate.now());
        if (setupPolicy.isPresent()) {
            log.info("Failed because attendance already initialized today");
            return ResponseEntity.badRequest().body(
                    InitializeAttendanceResponse.builder()
                            .status("FAILED")
                            .message("attendance already initialized TODAY!")
                            .metaData(InitializeAttendanceResponse.Metadata.builder()
                                    .subjectId(setupPolicy.get().getSubjectId())
                                    .attendanceCode(setupPolicy.get().getCode())
                                    .totalDurationInMinutes(setupPolicy.get().getDuration()+"")
                                    .build())
                            .build());
        }
        log.info("Duration => {}",duration);
        if (duration < 10) {
            log.info("Failed because Duration less than 10MIN");
            return ResponseEntity.badRequest().body(
                    InitializeAttendanceResponse.builder()
                            .status("FAILED")
                            .message("Duration at least 10 minutes")
                            .build());
        }
        String jwtToken = jwtService.extractTokenFromHeader(authorization);
        String id = jwtService.getId(jwtToken);
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            log.info("Failed because Subject not found");
            return new ResponseEntity<>(InitializeAttendanceResponse.builder()
                    .status("FAILED")
                    .message("Subject not found")
                    .build(),HttpStatus.NOT_FOUND);
        }
        Subject subject = subjectOptional.get();
        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            log.info("Failed because user is not authorized to take attendance");
            return new ResponseEntity<>(InitializeAttendanceResponse.builder()
                    .status("FAILED")
                    .message("Unauthorized to take attendance")
                    .build(),HttpStatus.UNAUTHORIZED);
        }
        Set<Student> allPossibleAttendees = studentService.getAllStudentsOfferingCourse(subjectCode);
        LocalDate localDate = LocalDate.now();
        List<Attendance> studentAttendance = allPossibleAttendees.stream()
                .map(student -> new Attendance(student.getMatriculationNumber(),
                        subject.getSubjectCode(),
                        localDate,
                        AttendanceStatus.ABSENT))
                .toList();
        log.info("Setting these students to absent {}",allPossibleAttendees);
        log.info("Build policy");
        AttendanceSetupPolicy setup = AttendanceSetupPolicy.builder()
                .code(UniqueCodeGenerator.generateCode(4))
                .attendanceDateTime(LocalDateTime.now())
                .duration(duration)
                .subjectId(subjectCode)
                .attendanceDate(localDate)
                .attendanceDateTime(LocalDateTime.now().plusMinutes(duration))
                .build();
        setup = attendanceSetupRepository.save(setup);
        attendanceRepository.saveAll(studentAttendance);
        log.info("Returning Success response");
        return new ResponseEntity<>(InitializeAttendanceResponse.builder()
                .status("SUCCESS")
                .message("Initialized attendance successfully")
                .metaData(InitializeAttendanceResponse.Metadata.builder()
                        .subjectId(setup.getSubjectId())
                        .attendanceCode(setup.getCode())
                        .creationDateTime(LocalDateTime.now().toString())
                        .expiryDateTime(LocalDateTime.now().plusMinutes(setup.getDuration()).toString())
                        .totalDurationInMinutes(String.valueOf(setup.getDuration()))
                        .build())
                .build(),HttpStatus.OK);
    }

    public ResponseEntity<String> initializeAttendance(String subjectCode, String authorization, int duration, LocalDate date) {
        log.info("Initializing attendance for subject code: {}, date: {}, duration: {}", subjectCode, date, duration);

        List<Attendance> attendances = attendanceRepository.findBySubjectIdAndDate(subjectCode, date);
        if (!attendances.isEmpty()) {
            log.warn("Attendance already initialized for subject code: {} on date: {}", subjectCode, date);
            return ResponseEntity.badRequest().body("Attendance already initialized");
        }

        if (duration < 10) {
            log.warn("Invalid duration: {}. Duration must be at least 10 minutes.", duration);
            return ResponseEntity.badRequest().body("Duration at least 10 minutes");
        }

        String id = jwtService.getId(authorization);
        log.info("Extracted user ID from authorization token: {}", id);

        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            log.warn("Subject not found for subject code: {}", subjectCode);
            return new ResponseEntity<>("Subject not found", HttpStatus.NOT_FOUND);
        }

        Subject subject = subjectOptional.get();
        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            log.warn("Unauthorized attempt to take attendance for subject code: {} by user ID: {}", subjectCode, id);
            return new ResponseEntity<>("Unauthorized to take attendance", HttpStatus.UNAUTHORIZED);
        }

        Set<Student> allPossibleAttendees = new HashSet<>(studentService.getAllStudentsOfferingCourse2(subjectCode));
        List<Attendance> studentAttendance = allPossibleAttendees.stream()
                .map(student -> new Attendance(student.getMatriculationNumber(),
                        subject.getSubjectCode(),
                        date,
                        AttendanceStatus.ABSENT))
                .toList();

        AttendanceSetupPolicy setup = AttendanceSetupPolicy.builder()
                .code(UniqueCodeGenerator.generateCode(10))
                .duration(duration)
                .subjectId(subjectCode)
                .attendanceDate(date)
                .attendanceDateTime(LocalDateTime.of(date, LocalTime.now()))
                .build();

        setup = attendanceSetupRepository.save(setup);
        log.info("Attendance setup created with code: {}", setup.getCode());

        attendanceRepository.saveAll(studentAttendance);
        log.info("Student attendance records saved for subject code: {}", subjectCode);

        return new ResponseEntity<>("code=" + setup.getCode(), HttpStatus.OK);
    }



    public ResponseEntity<String> updateAttendanceStatus(String attendanceCode, MultipartFile multipartFile) {
        log.info("Updating attendance status: attendanceCode={}", attendanceCode);
        Optional<AttendanceSetupPolicy> attendanceSetup = attendanceSetupRepository.findAll().stream()
                .filter(policy -> policy.getAttendanceDate().equals(LocalDate.now()) &&
                        policy.getAttendanceDateTime().plusMinutes(policy.getDuration()).isAfter(LocalDateTime.now()))
                .findAny();
        if (attendanceSetup.isEmpty()) {
            log.warn("No attendance setup found for today.");
            return ResponseEntity.badRequest().body("Attendance is not initialized yet");
        }
        AttendanceSetupPolicy policy = attendanceSetup.get();
        if (LocalDateTime.now().isAfter(policy.getAttendanceDateTime().plusMinutes(policy.getDuration()))) {
            log.warn("Attendance marking time has expired.");
            return ResponseEntity.badRequest().body("Time Expired");
        }
        String subjectCode = policy.getSubjectId();
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            log.warn("Subject not found for code: {}", subjectCode);
            return new ResponseEntity<>("Subject not found", HttpStatus.BAD_REQUEST);
        }
        try {
            ResponseEntity<Student> matriculationNumberResponse = faceRecognitionService.recognizeFace(multipartFile, subjectCode);
            Student student = matriculationNumberResponse.getBody();
            if (student == null) {
                log.warn("Student not recognized.");
                return new ResponseEntity<>("Student not recognized", HttpStatus.NOT_FOUND);
            }
            Optional<Suspension> isSuspended = suspensionRepository.findByStudentIdAndSubjectId(student.getMatriculationNumber(), subjectCode);
            if (isSuspended.isPresent()) {
                log.warn("Student is suspended.");
                return new ResponseEntity<>("Student suspended", HttpStatus.FORBIDDEN);
            }
            Attendance attendance = attendanceRepository.findByStudentIdAndSubjectIdAndDate(student.getMatriculationNumber(), subjectCode, LocalDate.now());
            if (attendance == null) {
                log.warn("Attendance record not found for studentId: {} and subjectId: {}", student.getMatriculationNumber(), subjectCode);
                return new ResponseEntity<>("Cannot mark attendance anymore", HttpStatus.FORBIDDEN);
            }
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                log.warn("Attendance already marked for studentId: {}", student.getMatriculationNumber());
                return new ResponseEntity<>("Already marked student", HttpStatus.CONFLICT);
            }
            attendance.setStatus(AttendanceStatus.PRESENT);
            attendanceRepository.save(attendance);
            log.info("Attendance successfully marked for studentId: {}", student.getMatriculationNumber());
            return new ResponseEntity<>("Successfully marked attendance: student Id=" + student.getMatriculationNumber(), HttpStatus.OK);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                log.error("Client error occurred: {}", ex.getMessage());
                return new ResponseEntity<>("Student not a member of the class", HttpStatus.NOT_FOUND);
            } else if (ex.getStatusCode().is5xxServerError()) {
                log.error("Server error occurred: {}", ex.getMessage());
                return new ResponseEntity<>("Error when processing file occurred", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error("Unexpected error occurred: ", e);
            return new ResponseEntity<>("Failed to mark attendance", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<AttendanceRecordResponse> getRecord(String subjectCode, LocalDate date, int sort,
            String bearer) {
        log.info("Received request to get attendance record for subjectCode: {}, date: {}, sort: {}, bearer: {}",
                subjectCode, date, sort, bearer);

        // Validate the subject
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            log.warn("Subject not found for subjectCode: {}", subjectCode);
            return ResponseEntity.badRequest().build();
        }

        Subject subject = subjectOptional.get();

        // Validate the lecturer's authorization
        String jwtToken = jwtService.extractTokenFromHeader(bearer);
        String userId = jwtService.getId(jwtToken);

        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(userId)) {
            log.warn("Unauthorized access attempt by userId: {} for subjectCode: {}", userId, subjectCode);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Fetch the attendance records
        List<Attendance> studentAttendance = attendanceRepository.findBySubjectIdAndDate(subjectCode, date);
        if (studentAttendance.isEmpty()) {
            log.info("No attendance records found for subjectCode: {}, date: {}", subjectCode, date);
            return ResponseEntity.notFound().build();
        }

        // Sort the records based on the provided parameter
        switch (sort) {
            case 1 -> studentAttendance = filterAttendanceByStatus(studentAttendance, AttendanceStatus.PRESENT);
            case 2 -> studentAttendance = filterAttendanceByStatus(studentAttendance, AttendanceStatus.ABSENT);
            case 0 -> {}
            default -> {
                log.warn("Invalid sort parameter: {}", sort);
                return ResponseEntity.badRequest().build();
            }
        }

        // Build the response
        AttendanceRecordResponse attendanceRecordResponse = buildAttendanceRecordResponse(subject, date, studentAttendance);
        log.info("Successfully fetched attendance record for subjectCode: {}, date: {}", subjectCode, date);
        return ResponseEntity.ok(attendanceRecordResponse);

    }

    public ResponseEntity<ByteArrayResource> getAttendanceExcel(String subjectCode, LocalDate date, int sort,
            String bearer) {
        log.info("Received request to generate attendance Excel for subjectCode: {}, date: {}, sort: {}, bearer: {}",
                subjectCode, date, sort, bearer);

        var record = getRecord(subjectCode, date, sort, bearer);
        if (!record.getStatusCode().equals(HttpStatus.OK)) {
            log.warn("Failed to get attendance records for subjectCode: {}, date: {}, sort: {}, bearer: {}",
                    subjectCode, date, sort, bearer);
            return ResponseEntity.badRequest().build();
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Records");
        Row headerRow = sheet.createRow(0);
        String[] headers = { "Matriculation Number", "Name", "Status" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        var body = Objects.requireNonNull(record.getBody()).getAttendanceData();
        for (var attendance : body) {
            Student student = studentService.getStudentById(attendance.getMatriculationNumber()).orElse(null);
            if (student == null) {
                log.warn("Student not found for matriculation number: {}", attendance.getMatriculationNumber());
                return ResponseEntity.badRequest().build();
            }
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendance.getMatriculationNumber());
            row.createCell(1).setCellValue(student.getLastname() + " " + student.getFirstname());
            row.createCell(2).setCellValue(attendance.getStatus().toString());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write workbook to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            log.info("Successfully written attendance records to Excel for subjectCode: {}, date: {}",
                    subjectCode, date);
        } catch (IOException e) {
            log.error("Error occurred while writing Excel file for subjectCode: {}, date: {}", subjectCode, date, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                workbook.close();
                log.info("Workbook closed successfully for subjectCode: {}, date: {}", subjectCode, date);
            } catch (IOException e) {
                log.error("Error occurred while closing the workbook for subjectCode: {}, date: {}", subjectCode, date, e);
            }
        }

        // Return the Excel file as ResponseEntity
        byte[] bytes = outputStream.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(bytes);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition", "attachment; filename=attendance_record_for_" + subjectCode + ".xlsx");

        log.info("Returning Excel file for subjectCode: {}, date: {}", subjectCode, date);
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .contentLength(bytes.length)
                .contentType(org.springframework.http.MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private List<Attendance> filterAttendanceByStatus(List<Attendance> attendanceList, AttendanceStatus status) {
        return attendanceList.stream()
                .filter(attendance -> attendance.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    private AttendanceRecordResponse buildAttendanceRecordResponse(Subject subject, LocalDate date,
            List<Attendance> attendanceList) {
        AttendanceRecordResponse attendanceRecordResponse = new AttendanceRecordResponse();
        attendanceRecordResponse.setTitle(subject.getSubjectTitle());
        attendanceRecordResponse.setSubjectCode(subject.getSubjectCode());
        attendanceRecordResponse.setDate(date.toString());
        attendanceRecordResponse.setAttendanceData(
                attendanceList.stream().map(v
                        -> {
                    Student student = studentService.getStudentById(v.getStudentId()).orElse(null);
                    if (student == null){
                        return null;
                    }
                    return AttendanceRecordResponse.MetaData.builder()
                            .firstname(student.getFirstname())
                            .lastname(student.getLastname())
                            .matriculationNumber(v.getStudentId())
                            .status(v.getStatus())
                            .build();
                }
                ).collect(Collectors.toList())
        );
        return attendanceRecordResponse;
    }

    public ResponseEntity<StudentAttendanceRecordResponse> viewAttendanceRecord(String bearer,String code) {
        log.info("Received request to get student attendance record for code: {} with bearer token", code);

        ResponseEntity<List<Attendance>> response = getStudentRecord(jwtService.getId(jwtService.extractTokenFromHeader(bearer)));
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to get student records: {}", response.getStatusCode());
            return ResponseEntity.notFound().build();
        }

        List<Attendance> attendanceList = response.getBody();
        if (attendanceList == null) {
            log.error("Attendance list is null for code: {}", code);
            return ResponseEntity.internalServerError().build();
        }

        log.info("Processing {} attendance records for subject code: {}", attendanceList.size(), code);

        List<StudentAttendanceRecordResponse.DefaultResponse> getDefault = attendanceList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getSubjectId().equalsIgnoreCase(code))
                .map(attendance -> {
                    Subject subject = subjectService.findSubjectByCode(attendance.getSubjectId()).orElse(null);
                    return subject != null
                            ? new StudentAttendanceRecordResponse.DefaultResponse(
                            subject.getSubjectCode(),
                            subject.getSubjectTitle(),
                            attendance.getDate(),
                            attendance.getStatus())
                            : null;
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Filtered and mapped {} records for subject code: {}", getDefault.size(), code);

        return ResponseEntity.ok(new StudentAttendanceRecordResponse(
                attendanceList.get(0).getStudentId(), getDefault));
    }

    public ResponseEntity<ByteArrayResource> printAttendanceRecord(String bearer, String code) {
        log.info("Received request to generate attendance Excel for subject code: {} with bearer token", code);
        // Call to fetch attendance record
        ResponseEntity<StudentAttendanceRecordResponse> response = viewAttendanceRecord(bearer, code);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to fetch attendance record: {}", response.getStatusCode());
            return ResponseEntity.notFound().build();
        }
        // Check if the body is null
        if (response.getBody() == null) {
            log.info("No content found for subject code: {}", code);
            return ResponseEntity.noContent().build();
        }
        log.info("Building Excel file for subject code: {}", code);
        // Proceed to build the Excel file
        ResponseEntity<ByteArrayResource> excelResponse = buildExcel(response.getBody());
        log.info("Excel file generated successfully for subject code: {}", code);
        return excelResponse;
    }

    private ResponseEntity<ByteArrayResource> buildExcel(StudentAttendanceRecordResponse body) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Record");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Student ID");
        headerRow.createCell(1).setCellValue("Subject ID");
        headerRow.createCell(2).setCellValue("Date");
        headerRow.createCell(3).setCellValue("Status");
        List<StudentAttendanceRecordResponse.DefaultResponse> attendanceRecord = body.getAttendanceRecord();
        int rowNum = 1;
        for (StudentAttendanceRecordResponse.DefaultResponse attendance : attendanceRecord) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(body.getStudentId());
            row.createCell(1).setCellValue(attendance.getSubjectId());
            row.createCell(2).setCellValue(attendance.getDate().format(DateTimeFormatter.ISO_DATE));
            row.createCell(3).setCellValue(attendance.getStatus().toString());
        }
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            log.error("Error occurred ", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("filename", "attendance_record.xlsx");
        return new ResponseEntity<>(new ByteArrayResource(outputStream.toByteArray()), headers, HttpStatus.OK);
    }
    public ResponseEntity<List<Attendance>> getStudentRecord(String studentId) {
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        if (attendances == null || attendances.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(attendances);
    }
    public ResponseEntity<AttendanceRecordHistoryResponse> getHistoryRecord(String subjectCode, String bearer){
        log.info("Request received to generate attendance history for subjectCode: {}", subjectCode);

        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            log.warn("Subject not found for subjectCode: {}", subjectCode);
            return ResponseEntity.badRequest().build();
        }
        Subject subject = subjectOptional.get();
        String jwtToken = jwtService.extractTokenFromHeader(bearer);
        String id = jwtService.getId(jwtToken);

        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            log.warn("Unauthorized access attempt for subjectCode: {} by userId: {}", subjectCode, id);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.info("Fetching attendance data for subjectCode: {}", subject.getSubjectCode());
        var data = attendanceRepository.findByStudentId(subject.getSubjectCode());
        final Map<String, Double> userScore = new HashMap<>();

        int total;
        try {
            total = Objects.requireNonNull(getRecord(subjectCode, bearer).getBody()).getData().size();
            log.info("Total attendance records retrieved: {}", total);
        } catch (Exception e) {
            log.error("Error fetching attendance records for subjectCode: {}", subjectCode, e);
            return ResponseEntity.notFound().build();
        }

        log.info("Calculating attendance scores for students...");
        data.forEach(attendance -> {
            userScore.computeIfPresent(attendance.getStudentId(),
                    (k, v) -> attendance.getStatus().equals(AttendanceStatus.PRESENT) ? v + 1.0 : v);
            userScore.putIfAbsent(attendance.getStudentId(),
                    attendance.getStatus().equals(AttendanceStatus.PRESENT) ? 1.0 : 0.0);
        });

        final int totalFinal = total;
        var keys = userScore.keySet();
        keys.forEach(key -> {
            userScore.compute(key, (k, v) -> v != null ? (100.0 * v) / totalFinal : 0);
        });

        log.info("Attendance score calculation completed. Generating response...");
        List<AttendanceRecordHistoryResponse.MetaData> metaDataList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : userScore.entrySet()) {
            Student student = studentService.getStudentById(entry.getKey()).get();
            AttendanceRecordHistoryResponse.MetaData metaData = AttendanceRecordHistoryResponse.MetaData.builder()
                    .firstname(student.getFirstname())
                    .lastname(student.getLastname())
                    .matriculationNumber(student.getMatriculationNumber())
                    .percentageAttendanceScore(String.format("%.2f", entry.getValue()) + "%")
                    .isEligibleForExam(entry.getValue() - 70.0 > 0.0001 ? "YES" : "NO")
                    .build();
            metaDataList.add(metaData);
        }

        AttendanceRecordHistoryResponse generateHistory = AttendanceRecordHistoryResponse.builder()
                .title(subject.getSubjectTitle())
                .subjectCode(subject.getSubjectCode())
                .attendanceData(metaDataList)
                .build();

        log.info("Attendance history generated successfully for subjectCode: {}", subjectCode);
        return ResponseEntity.ok(generateHistory);
    }
    public ResponseEntity<AvailableRecords> getRecord(String subjectCode, String bearer) {
        log.info("Received request to get record for subjectCode: {}", subjectCode);

        // Check if the subject exists
        Optional<Subject> subjectOptional = subjectService.findSubjectByCode(subjectCode);
        if (subjectOptional.isEmpty()) {
            log.warn("Subject not found for subjectCode: {}", subjectCode);
            return ResponseEntity.badRequest().build();
        }
        Subject subject = subjectOptional.get();

        // Extract and validate the JWT token
        String jwtToken = jwtService.extractTokenFromHeader(bearer);
        String id = jwtService.getId(jwtToken);
        if (subject.getLecturerInCharge() == null || !subject.getLecturerInCharge().getId().equals(id)) {
            log.warn("Unauthorized access attempt for subjectCode: {} by userId: {}", subjectCode, id);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Retrieve the attendance setup policies
        log.info("Retrieving attendance setup policies for subjectCode: {}", subjectCode);
        List<AttendanceSetupPolicy> attendanceSetupPolicyList = attendanceSetupRepository.findAllBySubjectId(subjectCode);

        if (attendanceSetupPolicyList.isEmpty()) {
            log.info("No attendance setup policies found for subjectCode: {}", subjectCode);
        } else {
            log.info("Found {} attendance setup policies for subjectCode: {}", attendanceSetupPolicyList.size(), subjectCode);
        }

        // Process the records
        log.info("Processing attendance setup policies");
        List<AvailableRecords.Data> set = attendanceSetupPolicyList.stream()
                .map(ob -> new AvailableRecords.Data(ob.getAttendanceDate().toString()))
                .toList();

        List<AvailableRecords.Data> sortedList = set.stream()
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate())))
                .toList();

        set = new ArrayList<>(sortedList);

        log.info("Processed and sorted attendance records. Number of records: {}", set.size());

        // Prepare and return the response
        AvailableRecords availableRecords = new AvailableRecords(set);
        log.info("Returning response with {} records for subjectCode: {}", set.size(), subjectCode);
        return ResponseEntity.ok(availableRecords);
    }
}
