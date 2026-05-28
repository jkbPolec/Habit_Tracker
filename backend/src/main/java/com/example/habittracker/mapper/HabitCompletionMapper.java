package com.example.habittracker.mapper;

import com.example.habittracker.dto.completion.HabitCompletionResponse;
import com.example.habittracker.entity.HabitCompletion;
import org.springframework.stereotype.Component;

@Component
public class HabitCompletionMapper {

    public HabitCompletionResponse toResponse(HabitCompletion completion) {
        return new HabitCompletionResponse(
                completion.getId(),
                completion.getHabit().getId(),
                completion.getCompletionDate(),
                completion.getCompletedAt(),
                completion.getNote()
        );
    }
}
