package com.backend.FaceRecognition.services.image_request_service;

import com.backend.FaceRecognition.entities.EncodedImages;
import com.backend.FaceRecognition.repository.EncodedImagesRepository;
import com.backend.FaceRecognition.utils.EncodedImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EncodingService {
    private final EncodedImagesRepository encodedImageRepository;
    /**
     * Retrieves the encoded images associated with a specific student.
     * This method retrieves the encoded images from the repository based on the provided matriculation number
     * of the student. It then maps the retrieved encoded images to a list of EncodedImageResponse objects containing
     * the matriculation number and the encoded image data.
     *
     * @param matriculation The matriculation number of the student whose encoded images are to be retrieved.
     * @return A ResponseEntity containing a list of EncodedImageResponse objects if the encoded images are found,
     *         or a NOT_FOUND status if no encoded images are associated with the provided matriculation number.
     */
    public ResponseEntity<List<EncodedImageResponse>> getStudentEncodings(String matriculation){
        List<EncodedImages> encodedImages = encodedImageRepository.findAllByMatriculationNumber(matriculation);
        if (encodedImages == null || encodedImages.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<EncodedImageResponse> responseList = encodedImages.stream()
                .map(eI -> new EncodedImageResponse(eI.getMatriculationNumber(), eI.getData()))
                .toList();
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }




}
