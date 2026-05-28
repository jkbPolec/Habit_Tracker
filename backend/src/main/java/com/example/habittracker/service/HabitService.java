package com.example.habittracker.service;

import com.example.habittracker.dto.completion.HabitCompletionRequest;
import com.example.habittracker.dto.completion.HabitCompletionResponse;
import com.example.habittracker.dto.habit.HabitCreateRequest;
import com.example.habittracker.dto.habit.HabitResponse;
import com.example.habittracker.dto.habit.HabitUpdateRequest;
import com.example.habittracker.dto.statistics.DashboardStatisticsResponse;
import com.example.habittracker.dto.statistics.HabitStatisticsResponse;
import com.example.habittracker.entity.Habit;
import com.example.habittracker.entity.HabitCompletion;
import com.example.habittracker.entity.User;
import com.example.habittracker.event.HabitCompletedEvent;
import com.example.habittracker.event.HabitCreatedEvent;
import com.example.habittracker.event.HabitDeletedEvent;
import com.example.habittracker.event.HabitUncompletedEvent;
import com.example.habittracker.event.HabitUpdatedEvent;
import com.example.habittracker.exception.DuplicateCompletionException;
import com.example.habittracker.exception.ForbiddenResourceException;
import com.example.habittracker.exception.InactiveHabitException;
import com.example.habittracker.exception.ResourceNotFoundException;
import com.example.habittracker.mapper.HabitCompletionMapper;
import com.example.habittracker.mapper.HabitMapper;
import com.example.habittracker.repository.HabitCompletionRepository;
import com.example.habittracker.repository.HabitRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;
    private final CurrentUserService currentUserService;
    private final HabitMapper habitMapper;
    private final HabitCompletionMapper completionMapper;
    private final ApplicationEventPublisher eventPublisher;

    public HabitService(
            HabitRepository habitRepository,
            HabitCompletionRepository completionRepository,
            CurrentUserService currentUserService,
            HabitMapper habitMapper,
            HabitCompletionMapper completionMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.habitRepository = habitRepository;
        this.completionRepository = completionRepository;
        this.currentUserService = currentUserService;
        this.habitMapper = habitMapper;
        this.completionMapper = completionMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> getHabits() {
        User user = currentUserService.getCurrentUser();
        return habitRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponseWithStats)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse getHabit(Long id) {
        return toResponseWithStats(getOwnedHabit(id));
    }

    @Transactional
    public HabitResponse createHabit(HabitCreateRequest request) {
        User user = currentUserService.getCurrentUser();
        Habit habit = new Habit();
        habit.setName(request.name());
        habit.setDescription(request.description());
        habit.setCategory(request.category());
        habit.setFrequency(request.frequency());
        habit.setTargetCount(request.targetCount());
        habit.setActive(true);
        habit.setUser(user);

        Habit saved = habitRepository.save(habit);
        eventPublisher.publishEvent(new HabitCreatedEvent(user.getId(), saved.getId(), saved.getName()));
        return toResponseWithStats(saved);
    }

    @Transactional
    public HabitResponse updateHabit(Long id, HabitUpdateRequest request) {
        Habit habit = getOwnedHabit(id);
        habit.setName(request.name());
        habit.setDescription(request.description());
        habit.setCategory(request.category());
        habit.setFrequency(request.frequency());
        habit.setTargetCount(request.targetCount());
        habit.setActive(request.active());

        Habit saved = habitRepository.save(habit);
        eventPublisher.publishEvent(new HabitUpdatedEvent(saved.getUser().getId(), saved.getId(), saved.getName()));
        return toResponseWithStats(saved);
    }

    @Transactional
    public void deleteHabit(Long id) {
        Habit habit = getOwnedHabit(id);
        Long userId = habit.getUser().getId();
        String name = habit.getName();
        completionRepository.deleteByHabitId(habit.getId());
        habitRepository.delete(habit);
        eventPublisher.publishEvent(new HabitDeletedEvent(userId, id, name));
    }

    @Transactional
    public HabitCompletionResponse completeHabit(Long id, HabitCompletionRequest request) {
        Habit habit = getOwnedHabit(id);
        if (!habit.isActive()) {
            throw new InactiveHabitException("Inactive habit cannot be completed");
        }
        if (completionRepository.existsByHabitIdAndCompletionDate(id, request.completionDate())) {
            throw new DuplicateCompletionException("Habit is already completed for this date");
        }

        HabitCompletion completion = new HabitCompletion();
        completion.setHabit(habit);
        completion.setCompletionDate(request.completionDate());
        completion.setNote(request.note());
        HabitCompletion saved = completionRepository.save(completion);
        eventPublisher.publishEvent(new HabitCompletedEvent(
                habit.getUser().getId(),
                habit.getId(),
                habit.getName(),
                request.completionDate()
        ));
        return completionMapper.toResponse(saved);
    }

    @Transactional
    public void uncompleteHabit(Long id, LocalDate date) {
        Habit habit = getOwnedHabit(id);
        HabitCompletion completion = completionRepository.findByHabitIdAndCompletionDate(id, date)
                .orElseThrow(() -> new ResourceNotFoundException("Completion not found"));
        completionRepository.delete(completion);
        eventPublisher.publishEvent(new HabitUncompletedEvent(habit.getUser().getId(), id, habit.getName(), date));
    }

    @Transactional(readOnly = true)
    public List<HabitCompletionResponse> getCompletions(Long id) {
        getOwnedHabit(id);
        return completionRepository.findByHabitIdOrderByCompletionDateDesc(id)
                .stream()
                .map(completionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HabitCompletionResponse> getCompletionsForMonth(Long id, int year, int month) {
        getOwnedHabit(id);
        YearMonth yearMonth = YearMonth.of(year, month);
        return completionRepository.findByHabitIdAndCompletionDateBetweenOrderByCompletionDateAsc(
                        id,
                        yearMonth.atDay(1),
                        yearMonth.atEndOfMonth()
                )
                .stream()
                .map(completionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardStatisticsResponse getDashboardStatistics() {
        User user = currentUserService.getCurrentUser();
        YearMonth currentMonth = YearMonth.now();
        long completions = completionRepository.countByHabitUserIdAndCompletionDateBetween(
                user.getId(),
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth()
        );
        return new DashboardStatisticsResponse(
                habitRepository.countByUserIdAndActiveTrue(user.getId()),
                completions
        );
    }

    @Transactional(readOnly = true)
    public HabitStatisticsResponse getHabitStatistics(Long id) {
        getOwnedHabit(id);
        return new HabitStatisticsResponse(
                id,
                calculateCurrentStreak(id),
                calculateBestStreak(id),
                completionRepository.existsByHabitIdAndCompletionDate(id, LocalDate.now())
        );
    }

    public int calculateCurrentStreak(Long habitId) {
        Set<LocalDate> dates = new HashSet<>(completionRepository.findCompletionDatesByHabitId(habitId));
        LocalDate cursor = dates.contains(LocalDate.now()) ? LocalDate.now() : LocalDate.now().minusDays(1);
        int streak = 0;
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public int calculateBestStreak(Long habitId) {
        List<LocalDate> dates = completionRepository.findCompletionDatesByHabitId(habitId).stream()
                .distinct()
                .sorted()
                .toList();
        int best = 0;
        int current = 0;
        LocalDate previous = null;
        for (LocalDate date : dates) {
            current = previous != null && date.equals(previous.plusDays(1)) ? current + 1 : 1;
            best = Math.max(best, current);
            previous = date;
        }
        return best;
    }

    private HabitResponse toResponseWithStats(Habit habit) {
        Long id = habit.getId();
        return habitMapper.toResponse(
                habit,
                completionRepository.existsByHabitIdAndCompletionDate(id, LocalDate.now()),
                calculateCurrentStreak(id),
                calculateBestStreak(id)
        );
    }

    private Habit getOwnedHabit(Long id) {
        User user = currentUserService.getCurrentUser();
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));
        if (!habit.getUser().getId().equals(user.getId())) {
            throw new ForbiddenResourceException("You do not have access to this habit");
        }
        return habit;
    }
}
