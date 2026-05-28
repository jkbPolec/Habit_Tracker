package com.example.habittracker.dto.statistics;

public record DashboardStatisticsResponse(
        long activeHabits,
        long currentMonthCompletions
) {
}
