package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.extras.ProfilePictureService;
import com.backend.FaceRecognition.utils.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/profile-picture")
public class ProfilePictureController {
        private final ProfilePictureService profilePictureService;
        public ProfilePictureController(ProfilePictureService profilePictureService) {
            this.profilePictureService = profilePictureService;
        }
        @GetMapping("/")
        public ResponseEntity<byte[]> getProfilePicture(@RequestHeader("Authorization") String bearer){
            return profilePictureService.getProfilePicture(bearer);
        }
        @PostMapping("/")
        public ResponseEntity<Response> upload(@RequestHeader("Authorization") String bearer, @RequestParam MultipartFile image){
            return profilePictureService.uploadProfilePicture(bearer, image);
        }


}
