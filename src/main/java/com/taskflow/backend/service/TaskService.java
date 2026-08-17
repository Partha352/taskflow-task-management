package com.taskflow.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.taskflow.backend.dto.TaskRequest;
import com.taskflow.backend.dto.TaskResponse;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.TaskPriority;
import com.taskflow.backend.entity.TaskStatus;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.UserPrincipal;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        UserPrincipal principal = currentPrincipal();

        User creator = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user no longer exists"
                ));

        Task task = new Task(
                request.title().trim(),
                normalizeDescription(request.description()),
                request.priority() == null
                        ? TaskPriority.MEDIUM
                        : request.priority(),
                request.dueDate(),
                creator
        );

        task.setStatus(
                request.status() == null
                        ? TaskStatus.TODO
                        : request.status()
        );

        return TaskResponse.from(taskRepository.save(task));
    }

    public List<TaskResponse> findAllForCurrentUser() {
        return findAllForCurrentUser(
                null,
                null,
                null,
                null,
                null
        );
    }

    public List<TaskResponse> findAllForCurrentUser(
            TaskStatus status,
            TaskPriority priority,
            Long assignedUserId,
            LocalDate dueDate,
            String search) {

        UserPrincipal principal = currentPrincipal();

        List<Task> tasks = taskRepository.findAll(
                buildTaskSpecification(
                        principal,
                        status,
                        priority,
                        assignedUserId,
                        dueDate,
                        search
                )
        );

        return tasks.stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse findById(Long taskId) {
        Task task = findTask(taskId);

        ensureCanView(task, currentPrincipal());

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(Long taskId, TaskRequest request) {
        Task task = findTask(taskId);

        ensureCanManage(task, currentPrincipal());

        task.setTitle(request.title().trim());

        task.setDescription(
                normalizeDescription(request.description())
        );

        task.setStatus(
                request.status() == null
                        ? task.getStatus()
                        : request.status()
        );

        task.setPriority(
                request.priority() == null
                        ? task.getPriority()
                        : request.priority()
        );

        task.setDueDate(request.dueDate());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long taskId) {
        Task task = findTask(taskId);

        ensureCanManage(task, currentPrincipal());

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse assign(Long taskId, Long userId) {
        UserPrincipal principal = currentPrincipal();

        if (!isAdmin(principal)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can assign tasks"
            );
        }

        Task task = findTask(taskId);

        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        task.setAssignedTo(assignee);

        return TaskResponse.from(taskRepository.save(task));
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));
    }

    /*
     * Checks whether the current user is allowed to view the task.
     */
    private void ensureCanView(Task task, UserPrincipal principal) {

        // ADMIN can view every task
        if (isAdmin(principal)) {
            return;
        }

        // Check task creator safely
        boolean isCreator =
                task.getCreatedBy() != null
                && task.getCreatedBy().getId() != null
                && task.getCreatedBy().getId().equals(principal.getId());

        // Check assignee safely
        boolean isAssignee =
                task.getAssignedTo() != null
                && task.getAssignedTo().getId() != null
                && task.getAssignedTo().getId().equals(principal.getId());

        if (isCreator || isAssignee) {
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to view this task"
        );
    }

    /*
     * Checks whether the current user is allowed to modify/delete the task.
     */
    private void ensureCanManage(Task task, UserPrincipal principal) {

        // ADMIN can manage every task
        if (isAdmin(principal)) {
            return;
        }

        // Check creator safely
        boolean isCreator =
                task.getCreatedBy() != null
                && task.getCreatedBy().getId() != null
                && task.getCreatedBy().getId().equals(principal.getId());

        if (isCreator) {
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to modify this task"
        );
    }

    private UserPrincipal currentPrincipal() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return principal;
    }

    private boolean isAdmin(UserPrincipal principal) {

        return principal.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );
    }

    private String normalizeDescription(String description) {

        return description == null || description.isBlank()
                ? null
                : description.trim();
    }

    private Specification<Task> buildTaskSpecification(
            UserPrincipal principal,
            TaskStatus status,
            TaskPriority priority,
            Long assignedUserId,
            LocalDate dueDate,
            String search) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            /*
             * ADMIN can see all tasks.
             *
             * Normal users can see:
             * 1. Tasks they created
             * 2. Tasks assigned to them
             */
            if (!isAdmin(principal)) {

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.equal(
                                        root.get("createdBy").get("id"),
                                        principal.getId()
                                ),
                                criteriaBuilder.equal(
                                        root.get("assignedTo").get("id"),
                                        principal.getId()
                                )
                        )
                );
            }

            if (status != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            if (priority != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("priority"),
                                priority
                        )
                );
            }

            if (assignedUserId != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("assignedTo").get("id"),
                                assignedUserId
                        )
                );
            }

            if (dueDate != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("dueDate"),
                                dueDate
                        )
                );
            }

            if (search != null && !search.isBlank()) {

                String expression =
                        "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("title")
                                        ),
                                        expression
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("description")
                                        ),
                                        expression
                                )
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }
}