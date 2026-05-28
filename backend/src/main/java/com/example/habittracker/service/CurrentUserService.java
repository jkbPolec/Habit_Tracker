package com.example.habittracker.service;

import com.example.habittracker.entity.User;
import com.example.habittracker.exception.ResourceNotFoundException;
import com.example.habittracker.repository.UserRepository;
import com.example.habittracker.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        String email = principal instanceof UserPrincipal userPrincipal
                ? userPrincipal.getEmail()
                : authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
