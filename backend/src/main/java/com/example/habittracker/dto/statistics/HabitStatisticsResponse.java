package com.example.habittracker.dto.statistics;

public record HabitStatisticsResponse(
        Long habitId,
        int currentStreak,
        int bestStreak,
        boolean completedToday
) {
}
