package com.backend.FaceRecognition.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@Getter
public class EncodeImageListResponse {
    @JsonProperty("matriculation_numbers")
    private List<String> matriculationNumbers;
    @JsonProperty("encodings")
    private List<double[]> encodings;
    public void add(String matriculationNumber, double[] encoding){
        if (matriculationNumbers == null || encodings == null){
            matriculationNumbers = new ArrayList<>();
            encodings= new ArrayList<>();
        }
        matriculationNumbers.add(matriculationNumber);
        encodings.add(encoding);
    }
}
