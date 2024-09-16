package com.backend.FaceRecognition.utils.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @JsonProperty("id")
    String id;
    @JsonProperty("password")
    String password;
}
