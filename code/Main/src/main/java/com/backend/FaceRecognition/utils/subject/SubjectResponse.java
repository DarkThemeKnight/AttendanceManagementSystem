package com.backend.FaceRecognition.utils.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SubjectResponse {
    private String message;
    @JsonProperty("subject_code")
    private String subjectCode;
    @JsonProperty("subject_title")
    private String subjectTitle;
    @JsonProperty("id_lecturer_in_charge")
    private String idLecturerInCharge;
    @JsonProperty("students")
    private List<Metadata> students = new ArrayList<>();
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class Metadata{
        String studentId;
        String firstname;
        String lastname;
        boolean isSuspended;
        String percentage;
    }
    public SubjectResponse(String message) {
        this.message = message;
    }
}

