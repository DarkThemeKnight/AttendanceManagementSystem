package com.backend.FaceRecognition.utils.history;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRecordHistoryResponse {
    @JsonProperty("Course_Title")
    String title;
    @JsonProperty("Course_code")
    String subjectCode;
    @JsonProperty("META_DATA")
    private List<MetaData> attendanceData;
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class MetaData{
        private String matriculationNumber;
        private String firstname;
        private String lastname;
        private String percentageAttendanceScore;
        private String isEligibleForExam;
    }
}
