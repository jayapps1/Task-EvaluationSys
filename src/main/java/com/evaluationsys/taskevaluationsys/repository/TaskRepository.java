package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.entity.enums.Quarter;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ===============================
    // FIND TASK BY CODE
    // ===============================
    Optional<Task> findByTaskCodeIgnoreCase(String taskCode);

    Optional<Task> findByTaskCode(String taskCode);

    List<Task> findByTaskCodeContainingIgnoreCase(String taskCode);

    // ===============================
    // FIND TASK BY DESCRIPTION
    // ===============================
    Optional<Task> findByDescription(String description);

    List<Task> findByDescriptionContainingIgnoreCase(String description);

    List<Task> findByTaskCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String code, String description);

    // ===============================
    // COUNT METHODS
    // ===============================
    long countByYear(Integer year);

    long count();  // ✅ ADDED

    long countByTaskStatus(TaskStatus status);  // ✅ ADDED

    @Query("SELECT COUNT(t) FROM Task t")
    long countAllTasks();  // ✅ ADDED

    // ===============================
    // FILTER BY TASK STATUS (ENUM)
    // ===============================
    List<Task> findByTaskStatus(TaskStatus status);

    List<Task> findByTaskStatusAndQuarterAndYear(TaskStatus status, Quarter quarter, Integer year);

    Page<Task> findByTaskStatus(TaskStatus status, Pageable pageable);  // ✅ ADDED

    // ===============================
    // FILTER BY TASK STATUS + SUPERVISOR
    // ===============================
    @Query("SELECT t FROM Task t WHERE t.taskStatus = :status AND t.supervisor.supervisorCode = :code")
    List<Task> findByTaskStatusAndSupervisorCode(@Param("status") TaskStatus status,
                                                 @Param("code") String supervisorCode);

    @Query("SELECT t FROM Task t WHERE t.supervisor.supervisorId = :supervisorId")
    List<Task> findBySupervisorId(@Param("supervisorId") Long supervisorId);  // ✅ ADDED

    // ===============================
    // FILTER BY QUARTER
    // ===============================
    List<Task> findByQuarter(Quarter quarter);

    List<Task> findByQuarterAndYear(Quarter quarter, Integer year);

    // ===============================
    // FILTER BY YEAR
    // ===============================
    List<Task> findByYear(Integer year);  // ✅ ADDED

    @Query("SELECT DISTINCT t.year FROM Task t ORDER BY t.year DESC")
    List<Integer> findDistinctYears();  // ✅ ADDED

    // ===============================
    // STATISTICS
    // ===============================
    @Query("SELECT t.taskStatus, COUNT(t) FROM Task t GROUP BY t.taskStatus")
    List<Object[]> countTasksGroupByStatus();  // ✅ ADDED

    @Query("SELECT t.quarter, COUNT(t) FROM Task t WHERE t.year = :year GROUP BY t.quarter")
    List<Object[]> countTasksByQuarter(@Param("year") Integer year);  // ✅ ADDED

    @Query("SELECT FUNCTION('MONTH', t.createdAt), COUNT(t) FROM Task t WHERE YEAR(t.createdAt) = :year GROUP BY FUNCTION('MONTH', t.createdAt)")
    List<Object[]> countTasksByMonth(@Param("year") Integer year);  // ✅ ADDED

    @Query("SELECT t.supervisor.department.departmentName, COUNT(t) FROM Task t WHERE t.supervisor.department IS NOT NULL GROUP BY t.supervisor.department.departmentName")
    List<Object[]> countTasksGroupByDepartment();  // ✅ ADDED

    // ===============================
    // RECENT TASKS
    // ===============================
    @Query("SELECT t FROM Task t ORDER BY t.createdAt DESC")
    List<Task> findRecentTasks();  // ✅ ADDED

    @Query(value = "SELECT * FROM task ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Task> findTopNTasks(@Param("limit") int limit);  // ✅ ADDED

    // ===============================
    // DATE RANGE QUERIES
    // ===============================
    @Query("SELECT t FROM Task t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Task> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);  // ✅ ADDED

    @Query("SELECT t FROM Task t WHERE t.deadline BETWEEN :startDate AND :endDate")
    List<Task> findByDeadlineBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);  // ✅ ADDED

    // ===============================
    // OVERDUE TASKS
    // ===============================
    @Query("SELECT t FROM Task t WHERE t.deadline < :now AND t.taskStatus NOT IN ('APPROVED', 'COMPLETED')")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);  // ✅ ADDED

    // ===============================
    // CREATED BY USER
    // ===============================
    List<Task> findByCreatedBy_StaffCode(Long staffCode);  // ✅ ADDED

    @Query("SELECT t FROM Task t WHERE t.createdBy.staffId = :staffId")
    List<Task> findByCreatedByStaffId(@Param("staffId") Long staffId);  // ✅ ADDED
}