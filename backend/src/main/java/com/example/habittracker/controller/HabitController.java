package com.example.habittracker.controller;

import com.example.habittracker.dto.completion.HabitCompletionRequest;
import com.example.habittracker.dto.completion.HabitCompletionResponse;
import com.example.habittracker.dto.habit.HabitCreateRequest;
import com.example.habittracker.dto.habit.HabitResponse;
import com.example.habittracker.dto.habit.HabitUpdateRequest;
import com.example.habittracker.dto.statistics.DashboardStatisticsResponse;
import com.example.habittracker.dto.statistics.DailyCompletionStatsResponse;
import com.example.habittracker.dto.statistics.HabitStatisticsResponse;
import com.example.habittracker.service.HabitService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public List<HabitResponse> getHabits() {
        return habitService.getHabits();
    }

    @GetMapping("/{id}")
    public HabitResponse getHabit(@PathVariable Long id) {
        return habitService.getHabit(id);
    }

    @PostMapping
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody HabitCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitService.createHabit(request));
    }

    @PutMapping("/{id}")
    public HabitResponse updateHabit(@PathVariable Long id, @Valid @RequestBody HabitUpdateRequest request) {
        return habitService.updateHabit(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/completions")
    public ResponseEntity<HabitCompletionResponse> completeHabit(
            @PathVariable Long id,
            @Valid @RequestBody HabitCompletionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitService.completeHabit(id, request));
    }

    @DeleteMapping("/{id}/completions/{date}")
    public ResponseEntity<Void> uncompleteHabit(
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        habitService.uncompleteHabit(id, date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/completions")
    public List<HabitCompletionResponse> getCompletions(@PathVariable Long id) {
        return habitService.getCompletions(id);
    }

    @GetMapping("/{id}/completions/month")
    public List<HabitCompletionResponse> getCompletionsForMonth(
            @PathVariable Long id,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return habitService.getCompletionsForMonth(id, year, month);
    }

    @GetMapping("/statistics")
    public DashboardStatisticsResponse getDashboardStatistics() {
        return habitService.getDashboardStatistics();
    }

    @GetMapping("/statistics/daily")
    public List<DailyCompletionStatsResponse> getDailyCompletionStatistics(
            @RequestParam(defaultValue = "14") int days
    ) {
        return habitService.getDailyCompletionStatistics(days);
    }

    @GetMapping("/{id}/statistics")
    public HabitStatisticsResponse getHabitStatistics(@PathVariable Long id) {
        return habitService.getHabitStatistics(id);
    }
}
