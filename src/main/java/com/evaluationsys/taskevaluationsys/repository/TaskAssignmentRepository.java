package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.TaskAssignment;
import com.evaluationsys.taskevaluationsys.entity.TaskAssignmentId;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, TaskAssignmentId> {

    // =========================
    // FIND BY COMPOSITE ID
    // =========================

    @Query("SELECT t FROM TaskAssignment t WHERE t.task.taskId = :taskId AND t.assignUser.staffId = :staffId")
    Optional<TaskAssignment> findByTaskIdAndStaffId(@Param("taskId") Long taskId, @Param("staffId") Long staffId);

    Optional<TaskAssignment> findByTaskAssignCode(String taskAssignCode);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TaskAssignment t WHERE t.task.taskId = :taskId AND t.assignUser.staffId = :staffId")
    boolean existsByTaskIdAndStaffId(@Param("taskId") Long taskId, @Param("staffId") Long staffId);

    @Transactional
    @Modifying
    @Query("DELETE FROM TaskAssignment t WHERE t.task.taskId = :taskId AND t.assignUser.staffId = :staffId")
    void deleteByTaskIdAndStaffId(@Param("taskId") Long taskId, @Param("staffId") Long staffId);

    // =========================
    // BASIC QUERIES
    // =========================

    Optional<TaskAssignment> findTopByOrderByTaskAssignCodeDesc();

    @Transactional
    @Modifying
    @Query("DELETE FROM TaskAssignment t WHERE t.taskAssignCode = :code")
    void deleteByTaskAssignCode(@Param("code") String code);

    Optional<TaskAssignment> findByTask_DescriptionAndAssignUser_StaffId(String description, Long staffId);

    // =========================
    // QUERIES BY TASK STATUS
    // =========================

    List<TaskAssignment> findByStatus(TaskStatus status);

    @Query("SELECT t FROM TaskAssignment t WHERE t.status = :status")
    List<TaskAssignment> findAllByStatus(@Param("status") TaskStatus status);

    // =========================
    // QUERIES BY TASK
    // =========================

    List<TaskAssignment> findByTask_TaskId(Long taskId);

    @Query("SELECT t FROM TaskAssignment t WHERE t.task.taskId = :taskId")
    List<TaskAssignment> findAllByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT t FROM TaskAssignment t WHERE t.task.taskId = :taskId AND t.status = :status")
    List<TaskAssignment> findByTaskIdAndStatus(@Param("taskId") Long taskId, @Param("status") TaskStatus status);

    // =========================
    // QUERIES BY USER/STAFF
    // =========================

    List<TaskAssignment> findByAssignUser_StaffId(Long staffId);

    List<TaskAssignment> findByAssignUser_StaffCode(Long staffCode);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffCode = :staffCode")
    List<TaskAssignment> findAllByStaffCode(@Param("staffCode") Long staffCode);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffId = :staffId")
    List<TaskAssignment> findAllByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffId = :staffId AND t.status = :status")
    List<TaskAssignment> findByStaffIdAndStatus(@Param("staffId") Long staffId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffCode = :staffCode AND t.status = :status")
    List<TaskAssignment> findByStaffCodeAndStatus(@Param("staffCode") Long staffCode, @Param("status") TaskStatus status);

    // =========================
    // COMPLEX QUERIES
    // =========================

    @Query("SELECT t FROM TaskAssignment t WHERE t.status IN :statuses")
    List<TaskAssignment> findByStatusIn(@Param("statuses") List<TaskStatus> statuses);

    // =========================
    // COUNT QUERIES
    // =========================

    long countByStatus(TaskStatus status);

    @Query("SELECT COUNT(t) FROM TaskAssignment t WHERE t.status = :status")
    long countAllByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM TaskAssignment t WHERE t.task.taskId = :taskId")
    long countByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COUNT(t) FROM TaskAssignment t WHERE t.assignUser.staffId = :staffId")
    long countByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT COUNT(t) FROM TaskAssignment t WHERE t.assignUser.staffCode = :staffCode")
    long countByStaffCode(@Param("staffCode") Long staffCode);

    // =========================
    // EXISTS QUERIES
    // =========================

    boolean existsByTask_TaskIdAndAssignUser_StaffId(Long taskId, Long staffId);

    boolean existsByTask_TaskIdAndAssignUser_StaffCode(Long taskId, Long staffCode);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TaskAssignment t WHERE t.task.taskId = :taskId AND t.assignUser.staffId = :staffId")
    boolean existsAssignment(@Param("taskId") Long taskId, @Param("staffId") Long staffId);

    // =========================
    // BULK UPDATE QUERIES
    // =========================

    @Transactional
    @Modifying
    @Query("UPDATE TaskAssignment t SET t.status = :newStatus WHERE t.taskAssignCode = :code")
    int updateStatusByCode(@Param("code") String code, @Param("newStatus") TaskStatus newStatus);

    @Transactional
    @Modifying
    @Query("UPDATE TaskAssignment t SET t.status = :newStatus WHERE t.task.taskId = :taskId")
    int updateStatusByTaskId(@Param("taskId") Long taskId, @Param("newStatus") TaskStatus newStatus);

    @Transactional
    @Modifying
    @Query("UPDATE TaskAssignment t SET t.status = :newStatus WHERE t.task.taskId = :taskId AND t.assignUser.staffId = :staffId")
    int updateStatusByTaskIdAndStaffId(@Param("taskId") Long taskId, @Param("staffId") Long staffId, @Param("newStatus") TaskStatus newStatus);

    @Transactional
    @Modifying
    @Query("UPDATE TaskAssignment t SET t.status = :newStatus WHERE t.task.taskId = :taskId AND t.assignUser.staffCode = :staffCode")
    int updateStatusByTaskIdAndStaffCode(@Param("taskId") Long taskId, @Param("staffCode") Long staffCode, @Param("newStatus") TaskStatus newStatus);

    @Transactional
    @Modifying
    @Query("UPDATE TaskAssignment t SET t.status = :newStatus WHERE t.assignUser.staffId = :staffId")
    int updateStatusByStaffId(@Param("staffId") Long staffId, @Param("newStatus") TaskStatus newStatus);

    @Transactional
    @Modifying
    @Query("UPDATE TaskAssignment t SET t.status = :newStatus WHERE t.assignUser.staffCode = :staffCode")
    int updateStatusByStaffCode(@Param("staffCode") Long staffCode, @Param("newStatus") TaskStatus newStatus);

    // =========================
    // DELETE QUERIES
    // =========================

    @Transactional
    @Modifying
    void deleteByTask_TaskId(Long taskId);

    @Transactional
    @Modifying
    void deleteByAssignUser_StaffId(Long staffId);

    @Transactional
    @Modifying
    void deleteByAssignUser_StaffCode(Long staffCode);

    @Transactional
    @Modifying
    @Query("DELETE FROM TaskAssignment t WHERE t.status = :status")
    void deleteByStatus(@Param("status") TaskStatus status);

    // =========================
    // DATE RANGE QUERIES
    // =========================

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignedAt BETWEEN :startDate AND :endDate")
    List<TaskAssignment> findByAssignedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                                 @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignedAt >= :startDate")
    List<TaskAssignment> findByAssignedAtAfter(@Param("startDate") java.time.LocalDateTime startDate);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignedAt <= :endDate")
    List<TaskAssignment> findByAssignedAtBefore(@Param("endDate") java.time.LocalDateTime endDate);

    // =========================
    // AGGREGATION QUERIES
    // =========================

    @Query("SELECT t.status, COUNT(t) FROM TaskAssignment t GROUP BY t.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT t.assignUser.staffId, COUNT(t) FROM TaskAssignment t GROUP BY t.assignUser.staffId")
    List<Object[]> countGroupByStaffId();

    @Query("SELECT t.assignUser.staffCode, COUNT(t) FROM TaskAssignment t GROUP BY t.assignUser.staffCode")
    List<Object[]> countGroupByStaffCode();

    @Query("SELECT t.task.taskId, COUNT(t) FROM TaskAssignment t GROUP BY t.task.taskId")
    List<Object[]> countGroupByTask();

    // =========================
    // DISTINCT QUERIES
    // =========================

    @Query("SELECT DISTINCT t.status FROM TaskAssignment t")
    List<TaskStatus> findDistinctStatuses();

    @Query("SELECT DISTINCT t.task.taskId FROM TaskAssignment t")
    List<Long> findDistinctTaskIds();

    @Query("SELECT DISTINCT t.assignUser.staffId FROM TaskAssignment t")
    List<Long> findDistinctStaffIds();

    @Query("SELECT DISTINCT t.assignUser.staffCode FROM TaskAssignment t")
    List<Long> findDistinctStaffCodes();

    // =========================
    // LATEST ASSIGNMENTS
    // =========================

    @Query("SELECT t FROM TaskAssignment t ORDER BY t.assignedAt DESC")
    List<TaskAssignment> findLatestAssignments();

    @Query(value = "SELECT * FROM task_assignment ORDER BY assigned_at DESC LIMIT :limit", nativeQuery = true)
    List<TaskAssignment> findTopNAssignments(@Param("limit") int limit);

    // =========================
    // PENDING REVIEW QUERIES
    // =========================

    @Query("SELECT t FROM TaskAssignment t WHERE t.status = 'PENDING_REVIEW'")
    List<TaskAssignment> findPendingReviews();

    @Query("SELECT t FROM TaskAssignment t WHERE t.status = 'PENDING_REVIEW' AND t.task.taskId = :taskId")
    List<TaskAssignment> findPendingReviewsByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT t FROM TaskAssignment t WHERE t.task.taskId = :taskId AND t.assignUser.staffId = :staffId AND t.status = 'PENDING_REVIEW'")
    Optional<TaskAssignment> findPendingReviewByTaskIdAndStaffId(@Param("taskId") Long taskId, @Param("staffId") Long staffId);

    // =========================
    // ACTIVE ASSIGNMENTS
    // =========================

    @Query("SELECT t FROM TaskAssignment t WHERE t.status NOT IN ('APPROVED', 'REJECTED')")
    List<TaskAssignment> findActiveAssignments();

    @Query("SELECT t FROM TaskAssignment t WHERE t.status IN ('ASSIGNED', 'INITIATED', 'IN_PROGRESS', 'COMPLETED', 'PENDING_REVIEW')")
    List<TaskAssignment> findInProgressAssignments();

    // =========================
    // USER-SPECIFIC ACTIVE ASSIGNMENTS
    // =========================

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffId = :staffId AND t.status NOT IN ('APPROVED', 'REJECTED')")
    List<TaskAssignment> findActiveAssignmentsByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffCode = :staffCode AND t.status NOT IN ('APPROVED', 'REJECTED')")
    List<TaskAssignment> findActiveAssignmentsByStaffCode(@Param("staffCode") Long staffCode);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffId = :staffId AND t.status IN ('ASSIGNED', 'INITIATED', 'IN_PROGRESS')")
    List<TaskAssignment> findCurrentTasksByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffCode = :staffCode AND t.status IN ('ASSIGNED', 'INITIATED', 'IN_PROGRESS')")
    List<TaskAssignment> findCurrentTasksByStaffCode(@Param("staffCode") Long staffCode);

    // =========================
    // ADDITIONAL BASIC QUERIES
    // =========================

    Optional<TaskAssignment> findByTask_DescriptionAndAssignUser_StaffCode(String description, Long staffCode);

    @Query("SELECT t FROM TaskAssignment t WHERE t.task.description = :description AND t.assignUser.staffCode = :staffCode")
    Optional<TaskAssignment> findByDescriptionAndStaffCode(@Param("description") String description, @Param("staffCode") Long staffCode);

    // =========================
    // DEPARTMENT QUERIES
    // =========================

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.department.departmentId = :departmentId")
    List<TaskAssignment> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.department.departmentId = :departmentId AND t.status = :status")
    List<TaskAssignment> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") TaskStatus status);

    // =========================
    // CRITICAL METHODS FOR STAFF EVALUATION - ADD THESE
    // =========================

    // Find by staffId (assignUser.staffId) - ALIAS for findByAssignUser_StaffId
    default List<TaskAssignment> findByStaffId(Long staffId) {
        return findByAssignUser_StaffId(staffId);
    }

    // Find by staffId and task taskId
    @Query("SELECT t FROM TaskAssignment t WHERE t.assignUser.staffId = :staffId AND t.task.taskId = :taskId")
    Optional<TaskAssignment> findByStaffIdAndTask_TaskId(@Param("staffId") Long staffId, @Param("taskId") Long taskId);

    // Find by task taskId (returns all assignments for a task)
    @Query("SELECT t FROM TaskAssignment t WHERE t.task.taskId = :taskId")
    List<TaskAssignment> findByTask_TaskIdList(@Param("taskId") Long taskId);
}