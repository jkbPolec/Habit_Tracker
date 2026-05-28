package com.example.habittracker.dto.completion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record HabitCompletionRequest(
        @NotNull(message = "Completion date is required")
        LocalDate completionDate,

        @Size(max = 300, message = "Note cannot exceed 300 characters")
        String note
) {
}
