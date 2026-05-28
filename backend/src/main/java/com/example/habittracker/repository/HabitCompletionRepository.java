package com.example.habittracker.repository;

import com.example.habittracker.entity.HabitCompletion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    Optional<HabitCompletion> findByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    List<HabitCompletion> findByHabitIdOrderByCompletionDateDesc(Long habitId);

    List<HabitCompletion> findByHabitIdAndCompletionDateBetweenOrderByCompletionDateAsc(
            Long habitId,
            LocalDate start,
            LocalDate end
    );

    long countByHabitUserIdAndCompletionDateBetween(Long userId, LocalDate start, LocalDate end);

    @Query("select c.completionDate from HabitCompletion c where c.habit.id = :habitId")
    List<LocalDate> findCompletionDatesByHabitId(@Param("habitId") Long habitId);

    void deleteByHabitId(Long habitId);
}
