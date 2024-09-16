package com.backend.FaceRecognition.repository;

import com.backend.FaceRecognition.entities.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordTokenSaltRepository extends JpaRepository<ResetPasswordToken, Integer>{
    Optional<ResetPasswordToken> findByUserId(String userId);
    Optional<ResetPasswordToken> findBySalt(String salt);
}
