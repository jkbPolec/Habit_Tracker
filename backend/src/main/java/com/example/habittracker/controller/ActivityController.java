package com.example.habittracker.controller;

import com.example.habittracker.dto.activity.ActivityLogResponse;
import com.example.habittracker.service.ActivityLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityLogService activityLogService;

    public ActivityController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<ActivityLogResponse> getActivity() {
        return activityLogService.getCurrentUserActivity();
    }
}
