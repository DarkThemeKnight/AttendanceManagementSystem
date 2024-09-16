package com.backend.FaceRecognition.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class EncodedImage {
    @JsonProperty("message")
    private String message;
    @JsonProperty("encoded_image")
    private double[] data;
}
