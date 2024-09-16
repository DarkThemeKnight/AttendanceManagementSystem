package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitializeAttendanceResponse {
    private String status;
    private String message;
    private Metadata metaData;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static  class  Metadata{
        private String subjectId;
        private String attendanceCode;
        private String creationDateTime;
        private String expiryDateTime;
        private String totalDurationInMinutes;
    }

}
