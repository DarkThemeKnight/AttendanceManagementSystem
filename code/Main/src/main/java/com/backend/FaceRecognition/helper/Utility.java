package com.backend.FaceRecognition.helper;

import com.backend.FaceRecognition.constants.AttendanceStatus;
import com.backend.FaceRecognition.entities.Attendance;
import com.backend.FaceRecognition.repository.AttendanceRepository;
import com.backend.FaceRecognition.utils.AttendanceStatsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
@Service
public class Utility {
    private final AttendanceRepository attendanceRepository;
    public Utility(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public ResponseEntity<AttendanceStatsDTO> generateAttendanceStatistics(String... subjects){
        BiFunction<String,String[],Boolean> contains= (value,array) -> {
            if (array == null || array.length == 0){
                return true;
            }
            return Arrays.stream(subjects).anyMatch(st-> st.equalsIgnoreCase(value));
        };

        List<Attendance> attendanceHistory = attendanceRepository.findAll();
        attendanceHistory  = attendanceHistory.stream()
                .filter(v-> contains.apply(v.getSubjectId(),subjects)) //filter if is part of subject list
                .collect(Collectors.toList());

        List<Attendance> holdFilters = attendanceHistory.stream()
                .filter(v-> v.getDate().isEqual(LocalDate.now())) //filter by today's date
                .toList(); //get list

        int presentToday = holdFilters.stream()
                .filter(v->v.getStatus().equals(AttendanceStatus.PRESENT)).toList().size();
        int absentToday = holdFilters.size() - presentToday;

        holdFilters = attendanceHistory.stream()
                .filter(v->v.getDate().isEqual(LocalDate.now().minusDays(1))) // yesterday
                .collect(Collectors.toList());

        int presentYesterday = holdFilters.stream()
                .filter(v->v.getStatus().equals(AttendanceStatus.PRESENT)).toList().size();
        int absentYesterday = holdFilters.size() - presentToday;

        holdFilters = attendanceHistory.stream()
                .filter(v->v.getDate().getMonth().equals(LocalDate.now().getMonth()))
                .collect(Collectors.toList());

        int presentThisMonthTotal = holdFilters.stream().filter(v->v.getStatus().equals(AttendanceStatus.PRESENT)).toList().size();
        holdFilters = attendanceHistory.stream().filter(v->v.getDate().getMonth().equals(LocalDate.now().minusMonths(1).getMonth())).collect(Collectors.toList());
        int presentLastMonthTotal = holdFilters.stream().filter(v->v.getStatus().equals(AttendanceStatus.PRESENT)).toList().size();

        BiFunction<Integer, Integer, String> calculatePercentageIncrease = (present, past) -> {
            try {
                return String.format("%.2f", ((present - past) / (past + 0.0)) * 100);
            }catch (Exception e){
                return "0";
            }
        };
        String percentageIncreasePresentToday = calculatePercentageIncrease.apply(presentToday,presentYesterday);
        String percentageIncreaseAbsentToday = calculatePercentageIncrease.apply(absentToday,absentYesterday);
        String percentageIncreasePresentForTheMonth = calculatePercentageIncrease.apply(presentThisMonthTotal,presentLastMonthTotal);

        return ResponseEntity.ok(AttendanceStatsDTO.builder()
                .percentageIncreasePresentToday(percentageIncreasePresentToday)
                .percentageIncreaseAbsentToday(percentageIncreaseAbsentToday)
                .percentageIncreasePresentForTheMonth(percentageIncreasePresentForTheMonth)
                .presentToday(String.valueOf(presentToday))
                .presentThisMonthTotal(String.valueOf(presentThisMonthTotal))
                .absentToday(String.valueOf(absentToday))
                .build());
    }

}
