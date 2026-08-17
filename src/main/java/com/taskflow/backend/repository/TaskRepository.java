package com.taskflow.backend.repository;

import com.taskflow.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findDistinctByCreatedByIdOrAssignedToId(Long createdById, Long assignedToId);
}
