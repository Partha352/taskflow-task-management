package com.taskflow.backend.dto;

import com.taskflow.backend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name cannot exceed 100 characters") String name,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
        Role role
) {
}
