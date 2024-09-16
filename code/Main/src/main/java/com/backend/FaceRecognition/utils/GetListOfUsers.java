package com.backend.FaceRecognition.utils;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import com.backend.FaceRecognition.utils.application_user.ApplicationUserRequest;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GetListOfUsers {
    private List<ApplicationUserRequest> data;
}