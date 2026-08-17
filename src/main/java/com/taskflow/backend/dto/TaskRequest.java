package com.taskflow.backend.dto;

import com.taskflow.backend.entity.TaskPriority;
import com.taskflow.backend.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "Task title is required") @Size(max = 200, message = "Task title cannot exceed 200 characters") String title,
        @Size(max = 2000, message = "Task description cannot exceed 2000 characters") String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate
) {
}
