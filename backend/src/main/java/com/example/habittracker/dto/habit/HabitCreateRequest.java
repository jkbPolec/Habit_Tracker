package com.example.habittracker.dto.habit;

import com.example.habittracker.entity.HabitCategory;
import com.example.habittracker.entity.HabitFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HabitCreateRequest(
        @NotBlank(message = "Habit name is required")
        @Size(min = 3, max = 80, message = "Habit name must be between 3 and 80 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @NotNull(message = "Category is required")
        HabitCategory category,

        @NotNull(message = "Frequency is required")
        HabitFrequency frequency,

        @NotNull(message = "Target count is required")
        @Min(value = 1, message = "Target count must be at least 1")
        Integer targetCount
) {
}
