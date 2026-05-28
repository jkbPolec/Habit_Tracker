package com.example.habittracker.service;

import com.example.habittracker.dto.activity.ActivityLogResponse;
import com.example.habittracker.entity.User;
import com.example.habittracker.mapper.ActivityLogMapper;
import com.example.habittracker.repository.ActivityLogRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    private final CurrentUserService currentUserService;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            ActivityLogMapper activityLogMapper,
            CurrentUserService currentUserService
    ) {
        this.activityLogRepository = activityLogRepository;
        this.activityLogMapper = activityLogMapper;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getCurrentUserActivity() {
        User user = currentUserService.getCurrentUser();
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 50))
                .stream()
                .map(activityLogMapper::toResponse)
                .toList();
    }
}
