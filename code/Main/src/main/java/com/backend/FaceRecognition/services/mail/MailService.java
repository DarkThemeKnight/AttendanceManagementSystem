package com.backend.FaceRecognition.services.mail;

import com.backend.FaceRecognition.entities.ApplicationUser;
import com.backend.FaceRecognition.entities.ResetPasswordToken;
import com.backend.FaceRecognition.repository.ResetPasswordTokenSaltRepository;
import com.backend.FaceRecognition.services.application_user.ApplicationUserService;
import com.backend.FaceRecognition.services.jwt_service.JwtService;
import com.backend.FaceRecognition.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
@Slf4j
@Service
public class MailService {
    private final ApplicationUserService applicationUserService;
    private final JavaMailSender javaMailSender;
    private final JwtService jwtService;
    private final ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository ;
    @Value("${spring.mail.username}")
    private String myEmail;

    public MailService(ApplicationUserService applicationUserService, JavaMailSender javaMailSender, JwtService jwtService, ResetPasswordTokenSaltRepository resetPasswordTokenSaltRepository) {
        this.applicationUserService = applicationUserService;
        this.javaMailSender = javaMailSender;
        this.jwtService = jwtService;
        this.resetPasswordTokenSaltRepository = resetPasswordTokenSaltRepository;
    }
    public Response sendForgotPasswordResetLink(String userId) {
        Optional<ApplicationUser> applicationUser = applicationUserService.findUser(userId);
        if (applicationUser.isEmpty()) {
            return new Response(HttpStatus.NOT_FOUND.name());
        }
        String email = applicationUser.get().getSchoolEmail();
        String firstName = applicationUser.get().getFirstname();
        String lastName = applicationUser.get().getLastname();
        String token= jwtService.generateTemporaryToken(new HashMap<>(),userId);
        ResetPasswordToken resetPasswordToken = ResetPasswordToken.builder()
                .salt(token)
                .expiryDateTime(LocalDateTime.now().plusMinutes(10))
                .userId(userId)
                .build();
        resetPasswordTokenSaltRepository.save(resetPasswordToken);
        String passwordResetLink = getResetLink(token);
        String subject = "Password Reset Request";
        String text = "Dear " + firstName + " " + lastName + ",\n\n"
                + "We have received a request to reset your password. Please click on the link below to reset your password:\n\n"
                + passwordResetLink + "\n\n"
                + "If you did not request this password reset, please ignore this email. Your account remains secure.\n\n"
                + "Thank you,\n"
                + "Your Organization Name";
        sendMessage(email, subject, text);
        return new Response(HttpStatus.OK.name());
    }
    private String getResetLink(String token) {
        return "https://myresetlink.com/"+token;
    }
    private void sendMessage(String email, String subject, String body){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(myEmail);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        javaMailSender.send(simpleMailMessage);
        log.info("Forgot password reset link sent to =>{}",email);
    }

}
