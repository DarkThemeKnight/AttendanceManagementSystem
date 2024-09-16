package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
public class EncodedImages {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String matriculationNumber;
    @Lob
    private double[] data;

    @Builder
    public EncodedImages(String matriculationNumber, double[] data) {
        this.matriculationNumber = matriculationNumber;
        this.data = data;
    }

}
