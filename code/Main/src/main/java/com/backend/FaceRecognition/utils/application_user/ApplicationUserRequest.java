package com.backend.FaceRecognition.utils.application_user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationUserRequest {
    private String accountStatus;
    private String id;
    @JsonProperty("firstName")
    private String firstname;
    @JsonProperty("lastName")
    private String lastname;
//    @JsonIgnore
    private String middleName;
    private String schoolEmail;
  //  @JsonIgnore
    private LocalDate dateOfBirth;
    //@JsonIgnore
    private String address;
    private String phoneNumber;
    //@JsonIgnore
    private String faculty;
    //@JsonIgnore
    private String department;
   // @JsonIgnore
    private String role;
}
