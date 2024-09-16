package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.face_recognition_service.FaceRecognitionService;
import com.backend.FaceRecognition.utils.EncodeImageListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/encodings")
public class ImagesController {
    private final FaceRecognitionService faceRecognitionService;

    public ImagesController(FaceRecognitionService faceRecognitionService) {
        this.faceRecognitionService = faceRecognitionService;
    }

    @GetMapping
    public ResponseEntity<EncodeImageListResponse> getEncodingsBySubject(@RequestParam("code") String code) {
        return faceRecognitionService.getEncodings(code);
    }
}
