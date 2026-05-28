package com.example.habittracker.mapper;

import com.example.habittracker.dto.habit.HabitResponse;
import com.example.habittracker.entity.Habit;
import org.springframework.stereotype.Component;

@Component
public class HabitMapper {

    public HabitResponse toResponse(Habit habit, boolean completedToday, int currentStreak, int bestStreak) {
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getCategory(),
                habit.getFrequency(),
                habit.getTargetCount(),
                habit.isActive(),
                habit.getCreatedAt(),
                habit.getUpdatedAt(),
                completedToday,
                currentStreak,
                bestStreak
        );
    }
}
