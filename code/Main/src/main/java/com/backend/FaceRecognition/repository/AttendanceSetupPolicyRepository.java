package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.AttendanceSetupPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSetupPolicyRepository extends JpaRepository<AttendanceSetupPolicy,String> {
    Optional<AttendanceSetupPolicy> findBySubjectIdAndAttendanceDate(String subjectId, LocalDate date);
    List<AttendanceSetupPolicy> findAllBySubjectId(String subjectId);
    Optional<AttendanceSetupPolicy> findByCode(String subjectCode);
}
