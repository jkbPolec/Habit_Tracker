package com.example.habittracker.event;

import java.time.LocalDate;

public record HabitCompletedEvent(Long userId, Long habitId, String habitName, LocalDate completionDate) {
}
