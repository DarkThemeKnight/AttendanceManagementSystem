package com.backend.FaceRecognition.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitializeAttendance {
    private String subjectCode;
    @JsonProperty("duration")
    private Integer duration;
}
