package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ListOfSubjects {
    private String lecturerName;
    private String lecturerID;
    private List<MetaData> data;
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class MetaData{
        private String subjectId;
        private String subjectTitle;
    }
}
