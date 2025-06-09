package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardDto {
    private List<String> recentActivities;
    private List<String> notifications;
}
