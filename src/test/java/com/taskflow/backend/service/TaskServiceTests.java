package com.taskflow.backend.service;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.taskflow.backend.dto.TaskRequest;
import com.taskflow.backend.dto.TaskResponse;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.TaskPriority;
import com.taskflow.backend.entity.TaskStatus;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDefaultsToMediumPriorityWhenNoneProvided() {
        User creator = new User("Creator", "creator@example.com", "hash", Role.USER);
        UserPrincipal principal = UserPrincipal.from(creator);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskService service = new TaskService(taskRepository, userRepository);
        TaskRequest request = new TaskRequest("My Task", "Description", null, null, null);
        TaskResponse response = service.create(request);

        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void createSetsCreatorFromAuthenticatedPrincipal() {
        User creator = new User("Creator", "creator@example.com", "hash", Role.USER);
        UserPrincipal principal = UserPrincipal.from(creator);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskService service = new TaskService(taskRepository, userRepository);
        TaskRequest request = new TaskRequest("My Task", "Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDate.of(2026, 12, 31));
        TaskResponse response = service.create(request);

        assertThat(response.createdBy().id()).isEqualTo(creator.getId());
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void regularUserCannotDeleteTaskTheyDidNotCreate() {
        User creator = new User("Creator", "creator@example.com", "hash", Role.USER);
        User actor = new User("Actor", "actor@example.com", "hash", Role.USER);
        UserPrincipal principal = UserPrincipal.from(actor);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        Task task = new Task("Task", "Desc", TaskPriority.MEDIUM, null, creator);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskService service = new TaskService(taskRepository, userRepository);
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not authorized");
        verify(taskRepository, never()).deleteById(1L);
    }

    @Test
    void adminCanAssignTaskToAnotherUser() {
        User admin = new User("Admin", "admin@example.com", "hash", Role.ADMIN);
        User assignee = new User("Assignee", "assignee@example.com", "hash", Role.USER);
        UserPrincipal principal = UserPrincipal.from(admin);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        Task task = new Task("Task", "Desc", TaskPriority.MEDIUM, null, assignee);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskService service = new TaskService(taskRepository, userRepository);
        TaskResponse response = service.assign(1L, 2L);

        assertThat(response.assignedTo().id()).isEqualTo(assignee.getId());
        verify(taskRepository).save(task);
    }
}