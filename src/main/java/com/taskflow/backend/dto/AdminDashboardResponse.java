package com.taskflow.backend.dto;

public record AdminDashboardResponse(
        long totalUsers,
        long adminUsers,
        long standardUsers,
        long totalTasks,
        long pendingTasks,
        long inProgressTasks,
        long completedTasks,
        long highPriorityTasks,
        long overdueTasks
) {
}
