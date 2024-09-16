package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, String> {
    ApplicationUser findBySchoolEmail(String email);
    Optional<ApplicationUser> findByIdAndPassword(String email,String password);
}
