package com.backend.FaceRecognition.constants;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent");
    private final String status;

    AttendanceStatus(String status) {
        this.status = status;
    }

}
