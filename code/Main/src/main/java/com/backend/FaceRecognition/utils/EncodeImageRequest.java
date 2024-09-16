package com.backend.FaceRecognition.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
@ToString
public class EncodeImageRequest {
    List<String> ids;
    List<double[]> encode;
    @JsonProperty("file_name")
    String filename;
}
