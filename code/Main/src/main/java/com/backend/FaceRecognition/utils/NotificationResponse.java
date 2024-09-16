package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationResponse {
    private String message;
    private List<CustomData> data;
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CustomData {
        private String title;
        private String content;
    }

}
