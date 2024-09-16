package com.backend.FaceRecognition.utils.student;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudentResponse {
    @JsonProperty("matriculation_number")
    private String matriculationNumber;
    @JsonProperty("email")
    private String schoolEmail;
    private String firstname;
    private String lastname;
    @JsonProperty("middle_name")
    private String middleName;
    private String faculty;
    private String department;

}
