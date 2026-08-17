package com.taskflow.backend.controller;

import com.taskflow.backend.dto.UserResponse;
import com.taskflow.backend.dto.UserUpdateRequest;
import com.taskflow.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }
    @GetMapping public ResponseEntity<List<UserResponse>> findAll(@RequestParam(required = false) String search) { return ResponseEntity.ok(userService.findAll(search)); }
    @GetMapping("/{userId}") public ResponseEntity<UserResponse> findById(@PathVariable Long userId) { return ResponseEntity.ok(userService.findById(userId)); }
    @PutMapping("/{userId}") public ResponseEntity<UserResponse> update(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) { return ResponseEntity.ok(userService.update(userId, request)); }
    @DeleteMapping("/{userId}") public ResponseEntity<Void> delete(@PathVariable Long userId) { userService.delete(userId); return ResponseEntity.noContent().build(); }
}
