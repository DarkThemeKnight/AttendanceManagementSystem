package com.backend.FaceRecognition.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EncodedImageResponse {
    @JsonProperty("matriculation_number")
    private String matriculationNumber;
    @JsonProperty("encoded_image_value")
    private double[] data;
}
