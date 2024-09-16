package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class FaceRecognitionEndpoints {
    Map<String, String> endpointMap;

    public String getEndpoint(String key) {
        return endpointMap.get(key);
    }
}
