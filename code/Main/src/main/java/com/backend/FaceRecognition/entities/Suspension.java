package com.backend.FaceRecognition.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Suspension {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String studentId;
    private String subjectId;
}

