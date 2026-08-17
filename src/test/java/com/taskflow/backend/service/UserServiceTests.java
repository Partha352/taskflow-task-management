package com.taskflow.backend.service;

import com.taskflow.backend.dto.UserUpdateRequest;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {
    @Mock private UserRepository userRepository;
    @Mock private TaskRepository taskRepository;

    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }

    @Test
    void regularUserCannotChangeAnotherUsersProfile() {
        User actor = new User("Actor", "actor@example.com", "hash", Role.USER);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(UserPrincipal.from(actor), null, UserPrincipal.from(actor).getAuthorities()));
        UserService service = new UserService(userRepository, taskRepository);

        assertThatThrownBy(() -> service.update(2L, new UserUpdateRequest("Other", "other@example.com", Role.ADMIN)))
                .hasMessageContaining("not authorized");
    }

    @Test
    void adminCanChangeAUsersRole() {
        User admin = new User("Admin", "admin@example.com", "hash", Role.ADMIN);
        User target = new User("User", "user@example.com", "hash", Role.USER);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(UserPrincipal.from(admin), null, UserPrincipal.from(admin).getAuthorities()));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserService service = new UserService(userRepository, taskRepository);
        assertThat(service.update(2L, new UserUpdateRequest("User", "user@example.com", Role.ADMIN)).role()).isEqualTo(Role.ADMIN);
    }
}
