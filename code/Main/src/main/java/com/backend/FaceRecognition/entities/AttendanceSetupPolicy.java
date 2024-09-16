package com.backend.FaceRecognition.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSetupPolicy {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private int id;
   private String code;
   @Column(name = "subject_id")
   private String subjectId;
   private int duration;
   private LocalDate attendanceDate;
   private LocalDateTime attendanceDateTime;
   @Builder
   public AttendanceSetupPolicy(String code, String subjectId, int duration, LocalDate attendanceDate, LocalDateTime attendanceDateTime) {
      this.code = code;
      this.subjectId = subjectId;
      this.duration = duration;
      this.attendanceDate = attendanceDate;
      this.attendanceDateTime = attendanceDateTime;
   }
}
