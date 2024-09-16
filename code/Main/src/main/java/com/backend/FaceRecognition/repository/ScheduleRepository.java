package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer>{
    List<Schedule> findAllByUserId(String userId);

}
