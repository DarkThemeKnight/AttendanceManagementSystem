package com.backend.FaceRecognition.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatsDTO {
    private String percentageIncreasePresentToday;
    private String percentageIncreaseAbsentToday;
    private String percentageIncreasePresentForTheMonth;
    private String presentToday;
    private String presentThisMonthTotal;
    private String absentToday;
}
