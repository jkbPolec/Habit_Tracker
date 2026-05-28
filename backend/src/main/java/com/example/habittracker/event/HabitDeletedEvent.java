package com.example.habittracker.event;

public record HabitDeletedEvent(Long userId, Long habitId, String habitName) {
}
