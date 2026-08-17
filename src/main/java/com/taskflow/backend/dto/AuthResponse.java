package com.taskflow.backend.dto;

import com.taskflow.backend.entity.Role;

public record AuthResponse(String token, String tokenType, Long userId, String name, String email, Role role) {
}
