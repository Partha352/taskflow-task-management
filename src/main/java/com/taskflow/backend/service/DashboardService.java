package com.taskflow.backend.service;

import com.taskflow.backend.dto.AdminDashboardResponse;
import com.taskflow.backend.dto.UserDashboardResponse;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.TaskPriority;
import com.taskflow.backend.entity.TaskStatus;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public DashboardService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public UserDashboardResponse userDashboard() {
        UserPrincipal principal = currentPrincipal();
        List<Task> tasks = taskRepository.findDistinctByCreatedByIdOrAssignedToId(principal.getId(), principal.getId());
        return new UserDashboardResponse(
                tasks.size(),
                countByStatus(tasks, TaskStatus.TODO),
                countByStatus(tasks, TaskStatus.IN_PROGRESS),
                countByStatus(tasks, TaskStatus.COMPLETED),
                tasks.stream().filter(task -> task.getPriority() == TaskPriority.HIGH).count(),
                countOverdue(tasks),
                tasks.stream().filter(task -> task.getCreatedBy().getId().equals(principal.getId())).count(),
                tasks.stream().filter(task -> task.getAssignedTo() != null
                        && task.getAssignedTo().getId().equals(principal.getId())).count()
        );
    }

    public AdminDashboardResponse adminDashboard() {
        ensureAdmin(currentPrincipal());
        List<Task> tasks = taskRepository.findAll();
        long admins = userRepository.findAll().stream().filter(user -> user.getRole() == Role.ADMIN).count();
        return new AdminDashboardResponse(
                userRepository.count(),
                admins,
                userRepository.count() - admins,
                tasks.size(),
                countByStatus(tasks, TaskStatus.TODO),
                countByStatus(tasks, TaskStatus.IN_PROGRESS),
                countByStatus(tasks, TaskStatus.COMPLETED),
                tasks.stream().filter(task -> task.getPriority() == TaskPriority.HIGH).count(),
                countOverdue(tasks)
        );
    }

    private long countByStatus(List<Task> tasks, TaskStatus status) {
        return tasks.stream().filter(task -> task.getStatus() == status).count();
    }

    private long countOverdue(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        return tasks.stream().filter(task -> task.getDueDate() != null
                        && task.getDueDate().isBefore(today)
                        && task.getStatus() != TaskStatus.COMPLETED)
                .count();
    }

    private UserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return principal;
    }

    private void ensureAdmin(UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access is required");
        }
    }
}
