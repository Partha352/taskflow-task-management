package com.taskflow.backend.dto;

public record UserDashboardResponse(
        long totalTasks,
        long pendingTasks,
        long inProgressTasks,
        long completedTasks,
        long highPriorityTasks,
        long overdueTasks,
        long createdTasks,
        long assignedTasks
) {
}
