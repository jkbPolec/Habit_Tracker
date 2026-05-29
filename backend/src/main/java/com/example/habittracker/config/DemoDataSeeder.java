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
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

    public DemoDataSeeder(
            UserRepository userRepository,
            HabitRepository habitRepository,
            HabitCompletionRepository completionRepository,
            ActivityLogRepository activityLogRepository,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager
    ) {
        this.userRepository = userRepository;
        this.habitRepository = habitRepository;
        this.completionRepository = completionRepository;
        this.activityLogRepository = activityLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        resetDatabase();

        User demoUser = new User();
        demoUser.setUsername("demo_user");
        demoUser.setEmail(DEMO_EMAIL);
        demoUser.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        User savedUser = userRepository.save(demoUser);

        Habit reading = createHabit(
                savedUser,
                "Czytanie przed snem",
                "Kilka stron ksiazki przed odlozeniem telefonu.",
                HabitCategory.STUDY,
                HabitFrequency.DAILY,
                15,
                true
        );
        Habit walking = createHabit(
                savedUser,
                "Poranny spacer",
                "Krotki spacer przed rozpoczeciem pracy.",
                HabitCategory.FITNESS,
                HabitFrequency.DAILY,
                10,
                true
        );
        Habit water = createHabit(
                savedUser,
                "Woda rano",
                "Szklanka wody zaraz po przebudzeniu.",
                HabitCategory.HEALTH,
                HabitFrequency.DAILY,
                7,
                true
        );
        Habit meditation = createHabit(
                savedUser,
                "Wieczorna medytacja",
                "Wstrzymane na tydzien przez bol kolana.",
                HabitCategory.PERSONAL,
                HabitFrequency.DAILY,
                5,
                false
        );

        LocalDate today = LocalDate.now();
        addCompletions(reading, today.minusDays(6), today.minusDays(1), date -> true, "Przeczytane przed snem");
        addCompletions(walking, today.minusDays(9), today.minusDays(1), date -> true, "Spacer po okolicy");
        addCompletions(water, today.minusDays(13), today.minusDays(7), date -> true, "Woda wypita po przebudzeniu");
        addCompletions(meditation, today.minusDays(20), today.minusDays(15), date -> true, "Dziesiec minut oddechu");

        createLog(savedUser, reading, ActivityEventType.HABIT_CREATED, "Utworzono habit: Czytanie przed snem");
        createLog(savedUser, walking, ActivityEventType.HABIT_CREATED, "Utworzono habit: Poranny spacer");
        createLog(savedUser, water, ActivityEventType.HABIT_CREATED, "Utworzono habit: Woda rano");
        createLog(savedUser, meditation, ActivityEventType.HABIT_CREATED, "Utworzono habit: Wieczorna medytacja");
        createLog(savedUser, meditation, ActivityEventType.HABIT_UPDATED, "Wstrzymano habit: Wieczorna medytacja. Powod: bol kolana");
        createLog(savedUser, reading, ActivityEventType.HABIT_COMPLETED, "Wykonano habit: Czytanie przed snem wczoraj");
        createLog(savedUser, walking, ActivityEventType.HABIT_COMPLETED, "Wykonano habit: Poranny spacer wczoraj");
    }

    private void resetDatabase() {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE activity_logs, habit_completions, habits, users RESTART IDENTITY CASCADE"
        ).executeUpdate();
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

    private void addCompletions(
            Habit habit,
            LocalDate start,
            LocalDate end,
            Predicate<LocalDate> shouldComplete,
            String note
    ) {
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (shouldComplete.test(cursor)) {
                HabitCompletion completion = new HabitCompletion();
                completion.setHabit(habit);
                completion.setCompletionDate(cursor);
                completion.setNote(note);
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
