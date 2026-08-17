package com.taskflow.backend.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTests {

    @Test
    void newTaskUsesMediumPriorityWhenNoneIsProvided() {
        Task task = new Task("Write documentation", null, null, null, null);

        assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }
}
