package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {
    private String message;
    private StudentData data;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentData {
        private byte[] profilePicture;
        private String name;
        private String matriculationNumber;
        private String email;
        private String phoneNumber;
        private Course[] courses;
        private String address;
        private String dateOfBirth;
        private String attendanceCount;
        private String totalPossible;
        private String attendanceScore;
        private String department;
        private String faculty;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Course {
        private String name;
        private String title;
    }
}