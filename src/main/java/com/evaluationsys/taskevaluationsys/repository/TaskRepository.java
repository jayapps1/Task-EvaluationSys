package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ===============================
    // FIND TASK BY CODE
    // ===============================
    Optional<Task> findByTaskCodeIgnoreCase(String taskCode);
    List<Task> findByTaskCodeContainingIgnoreCase(String taskCode);

    // ===============================
    // FIND TASK BY DESCRIPTION
    // ===============================
    Optional<Task> findByDescription(String description);
    List<Task> findByDescriptionContainingIgnoreCase(String description);
    List<Task> findByTaskCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String code, String description);

    // ===============================
    // COUNT TASKS BY YEAR
    // ===============================
    long countByYear(Integer year);

    // ===============================
    // FILTER BY TASK STATUS (ENUM)
    // ===============================
    List<Task> findByTaskStatus(TaskStatus status);
    List<Task> findByTaskStatusAndQuarterAndYear(TaskStatus status, Quarter quarter, Integer year);

    // ===============================
    // FILTER BY TASK STATUS + SUPERVISOR
    // ===============================
    @Query("SELECT t FROM Task t WHERE t.taskStatus = :status AND t.supervisor.supervisorCode = :code")
    List<Task> findByTaskStatusAndSupervisorCode(@Param("status") TaskStatus status,
                                                 @Param("code") String supervisorCode);

    // ===============================
    // FILTER BY QUARTER
    // ===============================
    List<Task> findByQuarter(Quarter quarter);
    List<Task> findByQuarterAndYear(Quarter quarter, Integer year);

    Optional<Task> findByTaskCode(String taskCode);
}