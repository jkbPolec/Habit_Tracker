package com.example.habittracker.dto.habit;

import com.example.habittracker.entity.HabitCategory;
import com.example.habittracker.entity.HabitFrequency;
import java.time.LocalDateTime;

public record HabitResponse(
        Long id,
        String name,
        String description,
        HabitCategory category,
        HabitFrequency frequency,
        Integer targetCount,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean completedToday,
        int currentStreak,
        int bestStreak
) {
}
