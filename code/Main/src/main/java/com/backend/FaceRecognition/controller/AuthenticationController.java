package com.backend.FaceRecognition.controller;

import com.backend.FaceRecognition.services.authentication_service.AuthenticationService;
import com.backend.FaceRecognition.utils.ResetPassword;
import com.backend.FaceRecognition.utils.Response;
import com.backend.FaceRecognition.utils.authentication.AuthenticationRequest;
import com.backend.FaceRecognition.utils.authentication.AuthenticationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/auth")
@Component
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return authenticationService.login(authenticationRequest);
    }

    @PutMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization") String bearerToken) {
        return authenticationService.logout(bearerToken);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response> forgotPassword(@RequestParam String userId) {
        return authenticationService.forgotPassword(userId);
    }

    @PutMapping("/updatePassword/{token}")
    public ResponseEntity<Response> update(@PathVariable("token") String token,
            @RequestBody ResetPassword resetPassword) {
        return authenticationService.resetPassword(token, resetPassword);
    }

    @PutMapping("/updatePassword")
    public ResponseEntity<Response> updatePassword(@RequestHeader("Authorization") String bearer,
            @RequestBody ResetPassword resetPassword) {
        return authenticationService.updatePassword(bearer, resetPassword);
    }

}
