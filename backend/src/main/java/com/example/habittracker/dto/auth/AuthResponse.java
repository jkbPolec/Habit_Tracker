package com.example.habittracker.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String email
) {
}
