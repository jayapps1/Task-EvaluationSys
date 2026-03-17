package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, TaskAssignmentId> {

    Optional<TaskAssignment> findTopByOrderByTaskAssignCodeDesc();
    // Find by taskAssignCode
    Optional<TaskAssignment> findByTaskAssignCode(String taskAssignCode);


    // Delete by code
    void deleteByTaskAssignCode(String taskAssignCode);
}