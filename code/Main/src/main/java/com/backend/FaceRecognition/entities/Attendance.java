package com.backend.FaceRecognition.entities;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id")
    private String studentId;
    @Column(name = "subject_id")
    private String subjectId;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "status")
    private AttendanceStatus status;
    public Attendance() {
        // Default constructor
    }
    public Attendance(String studentId, String subjectId, LocalDate date, AttendanceStatus status) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.date = date;
        this.status = status;
    }
}
