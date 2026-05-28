package com.example.habittracker.event;

import com.example.habittracker.entity.ActivityEventType;
import com.example.habittracker.entity.ActivityLog;
import com.example.habittracker.entity.User;
import com.example.habittracker.repository.ActivityLogRepository;
import com.example.habittracker.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ActivityLogListener {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLogListener(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void onHabitCreated(HabitCreatedEvent event) {
        save(event.userId(), event.habitId(), ActivityEventType.HABIT_CREATED, "Created habit: " + event.habitName());
    }

    @EventListener
    @Transactional
    public void onHabitUpdated(HabitUpdatedEvent event) {
        save(event.userId(), event.habitId(), ActivityEventType.HABIT_UPDATED, "Updated habit: " + event.habitName());
    }

    @EventListener
    @Transactional
    public void onHabitDeleted(HabitDeletedEvent event) {
        save(event.userId(), event.habitId(), ActivityEventType.HABIT_DELETED, "Deleted habit: " + event.habitName());
    }

    @EventListener
    @Transactional
    public void onHabitCompleted(HabitCompletedEvent event) {
        save(
                event.userId(),
                event.habitId(),
                ActivityEventType.HABIT_COMPLETED,
                "Completed habit: " + event.habitName() + " on " + event.completionDate()
        );
    }

    @EventListener
    @Transactional
    public void onHabitUncompleted(HabitUncompletedEvent event) {
        save(
                event.userId(),
                event.habitId(),
                ActivityEventType.HABIT_UNCOMPLETED,
                "Removed completion for habit: " + event.habitName() + " on " + event.completionDate()
        );
    }

    private void save(Long userId, Long habitId, ActivityEventType eventType, String message) {
        User user = userRepository.findById(userId).orElseThrow();
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setHabitId(habitId);
        log.setEventType(eventType);
        log.setMessage(message);
        activityLogRepository.save(log);
    }
}
