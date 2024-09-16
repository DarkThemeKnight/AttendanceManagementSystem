package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Integer> {

    Optional<Suspension> findByStudentIdAndSubjectId(String studentId, String subjectId);
}