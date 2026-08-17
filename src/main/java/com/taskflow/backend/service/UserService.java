package com.taskflow.backend.service;

import com.taskflow.backend.dto.UserResponse;
import com.taskflow.backend.dto.UserUpdateRequest;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public UserService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public List<UserResponse> findAll(String search) {
        ensureAdmin();
        List<User> users = search == null || search.isBlank() ? userRepository.findAll()
                : userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim());
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(Long userId) { ensureAdmin(); return UserResponse.from(findUser(userId)); }

    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        UserPrincipal principal = currentPrincipal();
        boolean admin = isAdmin(principal);
        if (!admin && !Objects.equals(principal.getId(), userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this user");
        User user = findUser(userId); String email = request.email().trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmail(email).filter(existing -> existing != user && !Objects.equals(existing.getId(), userId))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered"); });
        user.setName(request.name().trim()); user.setEmail(email);
        if (admin && request.role() != null) user.setRole(request.role());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long userId) {
        UserPrincipal principal = currentPrincipal(); ensureAdmin();
        if (principal.getId().equals(userId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        User user = findUser(userId);
        if (!taskRepository.findDistinctByCreatedByIdOrAssignedToId(userId, userId).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a user with related tasks");
        userRepository.delete(user);
    }

    private User findUser(Long userId) { return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")); }
    private void ensureAdmin() { if (!isAdmin(currentPrincipal())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access is required"); }
    private UserPrincipal currentPrincipal() { Authentication a = SecurityContextHolder.getContext().getAuthentication(); if (a == null || !(a.getPrincipal() instanceof UserPrincipal p)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required"); return p; }
    private boolean isAdmin(UserPrincipal principal) { return principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); }
}
