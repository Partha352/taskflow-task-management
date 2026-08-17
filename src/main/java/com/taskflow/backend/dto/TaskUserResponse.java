package com.taskflow.backend.dto;

import com.taskflow.backend.entity.User;

public record TaskUserResponse(Long id, String name, String email) {

    public static TaskUserResponse from(User user) {
        return user == null ? null : new TaskUserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
