package com.taskflow.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.repository.TaskRepository;

@SpringBootTest
@ActiveProfiles("test")
class TaskFlowApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void contextLoads() {
    }
}
