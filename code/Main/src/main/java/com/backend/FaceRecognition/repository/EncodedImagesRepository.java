package com.backend.FaceRecognition.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.FaceRecognition.entities.EncodedImages;
import java.util.List;

@Repository
public interface EncodedImagesRepository extends JpaRepository<EncodedImages, Integer> {
    List<EncodedImages> findAllByMatriculationNumber(String matriculationNumber);
    List<EncodedImages> findAllByMatriculationNumberIn(List<String> matriculationNumbers);
}
