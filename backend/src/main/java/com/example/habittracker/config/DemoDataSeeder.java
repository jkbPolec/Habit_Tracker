package com.example.habittracker.config;

import com.example.habittracker.entity.ActivityEventType;
import com.example.habittracker.entity.ActivityLog;
import com.example.habittracker.entity.Habit;
import com.example.habittracker.entity.HabitCategory;
import com.example.habittracker.entity.HabitCompletion;
import com.example.habittracker.entity.HabitFrequency;
import com.example.habittracker.entity.User;
import com.example.habittracker.repository.ActivityLogRepository;
import com.example.habittracker.repository.HabitCompletionRepository;
import com.example.habittracker.repository.HabitRepository;
import com.example.habittracker.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.function.Predicate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("seed")
public class DemoDataSeeder implements CommandLineRunner {

    public static final String DEMO_EMAIL = "demo@example.com";
    public static final String DEMO_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            UserRepository userRepository,
            HabitRepository habitRepository,
            HabitCompletionRepository completionRepository,
            ActivityLogRepository activityLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.habitRepository = habitRepository;
        this.completionRepository = completionRepository;
        this.activityLogRepository = activityLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return;
        }

        User demoUser = new User();
        demoUser.setUsername("demo");
        demoUser.setEmail(DEMO_EMAIL);
        demoUser.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        User savedUser = userRepository.save(demoUser);

        Habit reading = createHabit(
                savedUser,
                "Read 20 pages",
                "Daily reading habit with a long streak.",
                HabitCategory.STUDY,
                HabitFrequency.DAILY,
                1,
                true
        );
        Habit running = createHabit(
                savedUser,
                "Morning run",
                "Run before work, mostly on weekdays.",
                HabitCategory.FITNESS,
                HabitFrequency.DAILY,
                1,
                true
        );
        Habit water = createHabit(
                savedUser,
                "Drink 2L water",
                "Simple health habit with many completions.",
                HabitCategory.HEALTH,
                HabitFrequency.DAILY,
                1,
                true
        );
        Habit planning = createHabit(
                savedUser,
                "Weekly planning",
                "Plan next week every Sunday.",
                HabitCategory.WORK,
                HabitFrequency.WEEKLY,
                1,
                true
        );
        Habit meditation = createHabit(
                savedUser,
                "Meditation",
                "Paused habit kept for demo filtering.",
                HabitCategory.PERSONAL,
                HabitFrequency.DAILY,
                1,
                false
        );

        LocalDate today = LocalDate.now();
        addCompletions(reading, today.minusDays(89), today, date -> !date.equals(today.minusDays(12)));
        addCompletions(running, today.minusDays(59), today, date ->
                date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY
                        && !date.equals(today.minusDays(8))
        );
        addCompletions(water, today.minusDays(44), today, date ->
                date.getDayOfMonth() % 6 != 0
                        && date.getDayOfMonth() % 13 != 0
        );
        addCompletions(planning, today.minusDays(84), today, date -> date.getDayOfWeek() == DayOfWeek.SUNDAY);
        addCompletions(meditation, today.minusDays(40), today.minusDays(16), date -> date.getDayOfMonth() % 4 != 0);

        createLog(savedUser, reading, ActivityEventType.HABIT_CREATED, "Created habit: Read 20 pages");
        createLog(savedUser, running, ActivityEventType.HABIT_CREATED, "Created habit: Morning run");
        createLog(savedUser, water, ActivityEventType.HABIT_CREATED, "Created habit: Drink 2L water");
        createLog(savedUser, planning, ActivityEventType.HABIT_CREATED, "Created habit: Weekly planning");
        createLog(savedUser, meditation, ActivityEventType.HABIT_UPDATED, "Updated habit: Meditation");
        createLog(savedUser, reading, ActivityEventType.HABIT_COMPLETED, "Completed habit: Read 20 pages on " + today);
        createLog(savedUser, water, ActivityEventType.HABIT_COMPLETED, "Completed habit: Drink 2L water on " + today);
    }

    private Habit createHabit(
            User user,
            String name,
            String description,
            HabitCategory category,
            HabitFrequency frequency,
            int targetCount,
            boolean active
    ) {
        Habit habit = new Habit();
        habit.setUser(user);
        habit.setName(name);
        habit.setDescription(description);
        habit.setCategory(category);
        habit.setFrequency(frequency);
        habit.setTargetCount(targetCount);
        habit.setActive(active);
        return habitRepository.save(habit);
    }

    private void addCompletions(Habit habit, LocalDate start, LocalDate end, Predicate<LocalDate> shouldComplete) {
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (shouldComplete.test(cursor)) {
                HabitCompletion completion = new HabitCompletion();
                completion.setHabit(habit);
                completion.setCompletionDate(cursor);
                completion.setNote("Seeded demo completion");
                completionRepository.save(completion);
            }
            cursor = cursor.plusDays(1);
        }
    }

    private void createLog(User user, Habit habit, ActivityEventType eventType, String message) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setHabitId(habit.getId());
        log.setEventType(eventType);
        log.setMessage(message);
        activityLogRepository.save(log);
    }
}
