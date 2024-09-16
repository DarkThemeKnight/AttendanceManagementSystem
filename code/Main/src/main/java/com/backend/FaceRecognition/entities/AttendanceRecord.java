package com.backend.FaceRecognition.entities;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRecord {
    private String subjectTitle;
    private String subjectCode;
    private LocalDate date;
    private Map<String,AttendanceStatus> studentMatriculationNumberList;
}
