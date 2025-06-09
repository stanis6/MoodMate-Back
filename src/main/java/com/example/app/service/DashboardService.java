package com.example.app.service;

import com.example.app.dto.ChildReportDto;
import com.example.app.dto.DashboardDto;
import com.example.app.models.Classroom;
import com.example.app.models.User;
import com.example.app.models.enums.UserRole;
import com.example.app.repository.ClassroomRepository;
import com.example.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ReportService reportService;

    public DashboardService(
            ClassroomRepository classroomRepository,
            UserRepository userRepository,
            ReportService reportService
    ) {
        this.classroomRepository = classroomRepository;
        this.userRepository      = userRepository;
        this.reportService       = reportService;
    }

    public DashboardDto getDashboardForTeacher(UUID teacherId) {
        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Classroom classroom = classroomRepository.findByTeacherId(teacherId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Classroom not found for teacher: " + teacherId));

        List<User> children = userRepository.findByClassroomIdAndRoleOrderByLastNameAsc(classroom.getId(), UserRole.CHILD);

        List<String> recentActivities = new ArrayList<>();

        for (User child : children) {
            UUID childId = child.getId();
            String childName = child.getFirstName() + " " + child.getLastName();

            ChildReportDto todayReport;
            try {
                todayReport = reportService.generateDailyReport(childId, today);
            } catch (Exception e) {
                todayReport = new ChildReportDto(childId, childName, today, List.of(), 0);
            }

            if (todayReport.getScore() > 0) {
                recentActivities.add(childName + " completed the daily game");
            }

            try {
                ChildReportDto yesterdayReport = reportService.generateDailyReport(childId, yesterday);
                if (todayReport.getScore() > yesterdayReport.getScore()) {
                    recentActivities.add(childName + " showed signs of improvement");
                }
            } catch (Exception e) {
            }
        }

        List<String> notifications = List.of(
                "Meeting scheduled with School Psychologist on " + today.plusDays(1),
                "New resources available for emotional learning"
        );

        return new DashboardDto(recentActivities, notifications);
    }
}
