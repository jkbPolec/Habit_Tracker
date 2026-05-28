package com.example.habittracker.dto.completion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HabitCompletionResponse(
        Long id,
        Long habitId,
        LocalDate completionDate,
        LocalDateTime completedAt,
        String note
) {
}
