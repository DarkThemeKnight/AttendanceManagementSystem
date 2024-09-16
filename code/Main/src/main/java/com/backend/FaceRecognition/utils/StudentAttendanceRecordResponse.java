package com.backend.FaceRecognition.utils;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAttendanceRecordResponse {
    @JsonProperty("student_id")
    private String studentId;
    @JsonProperty("record")
    private List<DefaultResponse> attendanceRecord;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DefaultResponse {
        @JsonProperty("subject_id")
        private String subjectId;
        private String subjectTitle;
        @JsonProperty("date")
        private LocalDate date;
        @JsonProperty("status")
        private AttendanceStatus status;
    }
}
