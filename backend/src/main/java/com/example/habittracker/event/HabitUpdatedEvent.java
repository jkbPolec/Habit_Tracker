package com.example.habittracker.event;

public record HabitUpdatedEvent(Long userId, Long habitId, String habitName) {
}
