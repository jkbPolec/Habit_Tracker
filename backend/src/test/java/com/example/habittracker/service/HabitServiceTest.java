package com.example.habittracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.example.habittracker.dto.completion.HabitCompletionRequest;
import com.example.habittracker.dto.habit.HabitCreateRequest;
import com.example.habittracker.dto.habit.HabitResponse;
import com.example.habittracker.dto.habit.HabitUpdateRequest;
import com.example.habittracker.entity.Habit;
import com.example.habittracker.entity.HabitCategory;
import com.example.habittracker.entity.HabitCompletion;
import com.example.habittracker.entity.HabitFrequency;
import com.example.habittracker.entity.User;
import com.example.habittracker.event.HabitCompletedEvent;
import com.example.habittracker.event.HabitCreatedEvent;
import com.example.habittracker.event.HabitDeletedEvent;
import com.example.habittracker.event.HabitUncompletedEvent;
import com.example.habittracker.event.HabitUpdatedEvent;
import com.example.habittracker.exception.DuplicateCompletionException;
import com.example.habittracker.exception.ForbiddenResourceException;
import com.example.habittracker.exception.ResourceNotFoundException;
import com.example.habittracker.mapper.HabitCompletionMapper;
import com.example.habittracker.mapper.HabitMapper;
import com.example.habittracker.repository.HabitCompletionRepository;
import com.example.habittracker.repository.HabitRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitCompletionRepository completionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private HabitService habitService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        habitService = new HabitService(
                habitRepository,
                completionRepository,
                currentUserService,
                new HabitMapper(),
                new HabitCompletionMapper(),
                eventPublisher
        );
        currentUser = user(1L);
        lenient().when(currentUserService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void createHabit_shouldCreateHabitForCurrentUser() {
        HabitCreateRequest request = new HabitCreateRequest(
                "Read 20 pages",
                "Evening reading",
                HabitCategory.STUDY,
                HabitFrequency.DAILY,
                1
        );
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> {
            Habit habit = invocation.getArgument(0);
            habit.setId(10L);
            return habit;
        });

        HabitResponse response = habitService.createHabit(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Read 20 pages");
        verify(habitRepository).save(any(Habit.class));
        verify(eventPublisher).publishEvent(any(HabitCreatedEvent.class));
    }

    @Test
    void getHabit_shouldReturnHabitWhenBelongsToUser() {
        Habit habit = habit(10L, currentUser);
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));

        HabitResponse response = habitService.getHabit(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Read 20 pages");
    }

    @Test
    void getHabit_shouldThrowExceptionWhenHabitDoesNotExist() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.getHabit(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Habit not found");
    }

    @Test
    void getHabit_shouldThrowExceptionWhenHabitBelongsToAnotherUser() {
        Habit habit = habit(10L, user(2L));
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));

        assertThatThrownBy(() -> habitService.getHabit(10L))
                .isInstanceOf(ForbiddenResourceException.class);
    }

    @Test
    void updateHabit_shouldUpdateHabit() {
        Habit habit = habit(10L, currentUser);
        HabitUpdateRequest request = new HabitUpdateRequest(
                "Morning run",
                "5 km",
                HabitCategory.FITNESS,
                HabitFrequency.DAILY,
                1,
                true
        );
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(habit)).thenReturn(habit);

        HabitResponse response = habitService.updateHabit(10L, request);

        assertThat(response.name()).isEqualTo("Morning run");
        assertThat(response.category()).isEqualTo(HabitCategory.FITNESS);
        verify(eventPublisher).publishEvent(any(HabitUpdatedEvent.class));
    }

    @Test
    void deleteHabit_shouldDeleteHabit() {
        Habit habit = habit(10L, currentUser);
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));

        habitService.deleteHabit(10L);

        verify(completionRepository).deleteByHabitId(10L);
        verify(habitRepository).delete(habit);
        verify(eventPublisher).publishEvent(any(HabitDeletedEvent.class));
    }

    @Test
    void completeHabit_shouldCreateCompletion() {
        Habit habit = habit(10L, currentUser);
        LocalDate today = LocalDate.now();
        HabitCompletionRequest request = new HabitCompletionRequest(today, "Done");
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));
        when(completionRepository.existsByHabitIdAndCompletionDate(10L, today)).thenReturn(false);
        when(completionRepository.save(any(HabitCompletion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        habitService.completeHabit(10L, request);

        verify(completionRepository).save(any(HabitCompletion.class));
        verify(eventPublisher).publishEvent(any(HabitCompletedEvent.class));
    }

    @Test
    void completeHabit_shouldThrowExceptionWhenCompletionAlreadyExists() {
        Habit habit = habit(10L, currentUser);
        LocalDate today = LocalDate.now();
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));
        when(completionRepository.existsByHabitIdAndCompletionDate(10L, today)).thenReturn(true);

        assertThatThrownBy(() -> habitService.completeHabit(10L, new HabitCompletionRequest(today, null)))
                .isInstanceOf(DuplicateCompletionException.class);
    }

    @Test
    void uncompleteHabit_shouldRemoveCompletion() {
        Habit habit = habit(10L, currentUser);
        HabitCompletion completion = new HabitCompletion();
        LocalDate today = LocalDate.now();
        completion.setHabit(habit);
        completion.setCompletionDate(today);
        when(habitRepository.findById(10L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletionDate(10L, today)).thenReturn(Optional.of(completion));

        habitService.uncompleteHabit(10L, today);

        verify(completionRepository).delete(completion);
        verify(eventPublisher).publishEvent(any(HabitUncompletedEvent.class));
    }

    @Test
    void calculateCurrentStreak_shouldReturnCorrectValue() {
        LocalDate today = LocalDate.now();
        when(completionRepository.findCompletionDatesByHabitId(10L)).thenReturn(List.of(
                today,
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(4)
        ));

        assertThat(habitService.calculateCurrentStreak(10L)).isEqualTo(3);
    }

    @Test
    void calculateBestStreak_shouldReturnCorrectValue() {
        LocalDate today = LocalDate.now();
        when(completionRepository.findCompletionDatesByHabitId(10L)).thenReturn(List.of(
                today.minusDays(7),
                today.minusDays(6),
                today.minusDays(3),
                today.minusDays(2),
                today.minusDays(1),
                today
        ));

        assertThat(habitService.calculateBestStreak(10L)).isEqualTo(4);
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPassword("password");
        return user;
    }

    private static Habit habit(Long id, User user) {
        Habit habit = new Habit();
        habit.setId(id);
        habit.setName("Read 20 pages");
        habit.setDescription("Evening reading");
        habit.setCategory(HabitCategory.STUDY);
        habit.setFrequency(HabitFrequency.DAILY);
        habit.setTargetCount(1);
        habit.setActive(true);
        habit.setUser(user);
        return habit;
    }
}
