package com.example.habittracker.dto.activity;

import com.example.habittracker.entity.ActivityEventType;
import java.time.LocalDateTime;

public record ActivityLogResponse(
        Long id,
        Long habitId,
        ActivityEventType eventType,
        String message,
        LocalDateTime createdAt
) {
}
