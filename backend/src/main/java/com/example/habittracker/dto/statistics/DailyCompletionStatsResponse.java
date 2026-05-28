package com.example.habittracker.dto.statistics;

import java.time.LocalDate;

public record DailyCompletionStatsResponse(
        LocalDate date,
        long completions
) {
}
