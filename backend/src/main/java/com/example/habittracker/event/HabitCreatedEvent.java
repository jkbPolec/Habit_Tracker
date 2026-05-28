package com.example.habittracker.event;

public record HabitCreatedEvent(Long userId, Long habitId, String habitName) {
}
