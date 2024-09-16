package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(String studentId);
    List<Attendance> findBySubjectIdAndDate(String subjectId, LocalDate startDate);
    Attendance findByStudentIdAndSubjectIdAndDate(String studentId, String subjectId, LocalDate startDate);
    List<Attendance>  findByStudentIdAndSubjectId(String studentId, String subjectId);
    List<Attendance> findBySubjectId(String subjectId);

}
