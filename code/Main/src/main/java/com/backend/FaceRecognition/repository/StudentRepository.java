package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public interface StudentRepository extends JpaRepository<Student,String> {
    @Query("SELECT DISTINCT s FROM Student s JOIN s.subjects subj WHERE subj.subjectCode = :subjectCode")
    Set<Student> findAllStudentsBySubjectCode(@Param("subjectCode") String subjectCode); // Change the return type to Set<Student>
    @Query("SELECT DISTINCT s FROM Student s JOIN s.subjects subj WHERE subj.subjectCode = :subjectCode")
    ArrayList<Student> findAllStudentsBySubjectCodeArrayList(@Param("subjectCode") String subjectCode); // Change the return type to Set<Student>

}
