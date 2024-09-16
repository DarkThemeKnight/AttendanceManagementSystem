package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String courseCode;
    private String courseTitle;
    private String duration;
    private String time;
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    private String userId;
}
