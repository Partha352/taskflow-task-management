package com.taskflow.backend.dto;

import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.TaskPriority;
import com.taskflow.backend.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        TaskUserResponse createdBy,
        TaskUserResponse assignedTo
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getPriority(),
                task.getDueDate(), task.getCreatedAt(), task.getUpdatedAt(),
                TaskUserResponse.from(task.getCreatedBy()), TaskUserResponse.from(task.getAssignedTo())
        );
    }
}
