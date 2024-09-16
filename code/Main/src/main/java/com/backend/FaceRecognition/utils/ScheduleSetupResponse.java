package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduleSetupResponse {
    private String message;
    List<CustomRequest> data;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomRequest{
        private int id;
        private String courseCode;
        private String courseTitle;
        private String duration;
        private String time;
        private DayOfWeek dayOfWeek;
    }
}
