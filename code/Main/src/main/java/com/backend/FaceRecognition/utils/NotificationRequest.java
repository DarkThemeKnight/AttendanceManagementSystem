package com.backend.FaceRecognition.utils;

        import com.fasterxml.jackson.annotation.JsonIgnore;
        import lombok.Data;
        import lombok.NoArgsConstructor;
        import lombok.NonNull;

        import java.time.LocalDate;

@NoArgsConstructor
@Data
public class NotificationRequest {
    @JsonIgnore(value = false)
    private String title;
    @JsonIgnore(value = false)
    private String content;
    @JsonIgnore(value = false)
    private String validUntil;
}
