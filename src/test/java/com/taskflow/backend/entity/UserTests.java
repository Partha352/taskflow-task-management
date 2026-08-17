package com.taskflow.backend.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTests {

    @Test
    void newUserDefaultsToUserRoleWhenNoRoleIsProvided() {
        User user = new User("Test User", "test@example.com", "hashed-password", null);

        assertThat(user.getRole()).isEqualTo(Role.USER);
    }
}
