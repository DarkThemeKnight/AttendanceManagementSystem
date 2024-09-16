package com.backend.FaceRecognition.utils.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {
    @JsonProperty( "subject_code")
    private String subjectCode;
    @JsonProperty("subject_title")
    private String subjectTitle;
    @JsonProperty("id_lecturer_in_charge")
    private String idLecturerInCharge;
}
