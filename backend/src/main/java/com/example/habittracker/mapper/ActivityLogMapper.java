package com.example.habittracker.mapper;

import com.example.habittracker.dto.activity.ActivityLogResponse;
import com.example.habittracker.entity.ActivityLog;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLogResponse toResponse(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getHabitId(),
                log.getEventType(),
                log.getMessage(),
                log.getCreatedAt()
        );
    }
}
