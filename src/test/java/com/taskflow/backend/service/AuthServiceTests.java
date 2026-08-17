package com.taskflow.backend.service;

import com.taskflow.backend.dto.RegisterRequest;
import com.taskflow.backend.dto.UserResponse;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import com.taskflow.backend.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Test
    void registerHashesThePasswordBeforeSaving() {
        AuthService authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
        when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(new RegisterRequest("Person", "Person@Example.com", "secret1"));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(userCaptor.capture());

        assertThat(response.email()).isEqualTo("person@example.com");
        assertThat(userCaptor.getValue().getPassword()).isNotEqualTo("secret1");
        assertThat(passwordEncoder.matches("secret1", userCaptor.getValue().getPassword())).isTrue();
    }

    @Test
    void registerRejectsAnExistingEmail() {
        AuthService authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
        when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Person", "person@example.com", "secret1")))
                .hasMessageContaining("Email is already registered");
    }
}
