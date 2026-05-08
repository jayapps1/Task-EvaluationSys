package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // =========================
    // BASIC QUERIES
    // =========================

    List<Evaluation> findByTask_TaskId(Long taskId);
    List<Evaluation> findByYear(Integer year);
    List<Evaluation> findByYearAndQuarter(Integer year, Integer quarter);
    long countByYear(Integer year);

    // =========================
    // STAFF ISOLATION QUERIES (NEW)
    // =========================

    // Get all evaluations for a specific staff member (COMPLETE ISOLATION)
    List<Evaluation> findByStaffId(Long staffId);

    // ✅ ADD THIS MISSING METHOD - Get evaluation by staff_id and task_id
    Optional<Evaluation> findByStaffIdAndTask_TaskId(Long staffId, Long taskId);

    // Get evaluations for a staff member ordered by date (most recent first)
    List<Evaluation> findByStaffIdOrderByEvaluationDateDesc(Long staffId);

    // Get evaluations for a staff member by year and quarter
    List<Evaluation> findByStaffIdAndYearAndQuarter(Long staffId, Integer year, Integer quarter);

    // Get evaluations for a staff member by year
    List<Evaluation> findByStaffIdAndYear(Long staffId, Integer year);

    // =========================
    // DUPLICATE CHECK QUERIES
    // =========================

    // Check if evaluation exists for specific task, year, and quarter
    @Query("SELECT COUNT(e) > 0 FROM Evaluation e WHERE e.task.taskId = :taskId AND e.year = :year AND e.quarter = :quarter")
    boolean existsByTask_TaskIdAndYearAndQuarter(@Param("taskId") Long taskId, @Param("year") Integer year, @Param("quarter") Integer quarter);

    // Check if evaluation exists for specific staff, task, year, and quarter (MOST ACCURATE)
    @Query("SELECT COUNT(e) > 0 FROM Evaluation e WHERE e.staffId = :staffId AND e.task.taskId = :taskId AND e.year = :year AND e.quarter = :quarter")
    boolean existsByStaffIdAndTaskIdAndYearAndQuarter(@Param("staffId") Long staffId, @Param("taskId") Long taskId, @Param("year") Integer year, @Param("quarter") Integer quarter);

    // Get evaluation by task, year, and quarter (may return multiple if tasks assigned to multiple staff)
    @Query("SELECT e FROM Evaluation e WHERE e.task.taskId = :taskId AND e.year = :year AND e.quarter = :quarter")
    List<Evaluation> findByTask_TaskIdAndYearAndQuarter(@Param("taskId") Long taskId, @Param("year") Integer year, @Param("quarter") Integer quarter);

    // =========================
    // AGGREGATION QUERIES
    // =========================

    // Get average score for a staff member
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.staffId = :staffId AND e.score IS NOT NULL")
    Double getAverageScoreByStaffId(@Param("staffId") Long staffId);

    // Get total evaluations count for a staff member
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.staffId = :staffId")
    Long countByStaffId(@Param("staffId") Long staffId);

    // Get evaluations by quarter for a staff member
    @Query("SELECT e.quarter, COUNT(e), AVG(e.score) FROM Evaluation e WHERE e.staffId = :staffId AND e.year = :year GROUP BY e.quarter ORDER BY e.quarter")
    List<Object[]> getEvaluationSummaryByQuarter(@Param("staffId") Long staffId, @Param("year") Integer year);

    // =========================
    // DATE RANGE QUERIES
    // =========================

    // Get evaluations between dates for a specific staff
    @Query("SELECT e FROM Evaluation e WHERE e.staffId = :staffId AND e.evaluationDate BETWEEN :startDate AND :endDate ORDER BY e.evaluationDate DESC")
    List<Evaluation> findByStaffIdAndEvaluationDateBetween(@Param("staffId") Long staffId,
                                                           @Param("startDate") java.time.LocalDateTime startDate,
                                                           @Param("endDate") java.time.LocalDateTime endDate);

    // Get recent evaluations for a staff member (limit)
    @Query(value = "SELECT * FROM evaluation WHERE staff_id = :staffId ORDER BY evaluation_date DESC LIMIT :limit", nativeQuery = true)
    List<Evaluation> findRecentByStaffId(@Param("staffId") Long staffId, @Param("limit") int limit);

    // =========================
    // DEPARTMENT/BRANCH AGGREGATION
    // =========================

    // Get average score for all staff in a department
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.staffId IN (SELECT u.staffId FROM User u WHERE u.department.departmentId = :departmentId) AND e.score IS NOT NULL")
    Double getAverageScoreByDepartment(@Param("departmentId") Long departmentId);

    // Get evaluation count by year and quarter for a department
    @Query("SELECT e.year, e.quarter, COUNT(e) FROM Evaluation e WHERE e.staffId IN (SELECT u.staffId FROM User u WHERE u.department.departmentId = :departmentId) GROUP BY e.year, e.quarter ORDER BY e.year DESC, e.quarter DESC")
    List<Object[]> getEvaluationCountByDepartmentGroupByQuarter(@Param("departmentId") Long departmentId);

    // =========================
    // PERFORMANCE LEVEL QUERIES
    // =========================

    // Get count of staff by performance level (based on average score)
    @Query("SELECT " +
            "CASE " +
            "  WHEN AVG(e.score) >= 90 THEN 'Excellent' " +
            "  WHEN AVG(e.score) >= 75 THEN 'Good' " +
            "  WHEN AVG(e.score) >= 60 THEN 'Satisfactory' " +
            "  WHEN AVG(e.score) >= 40 THEN 'Needs Improvement' " +
            "  WHEN AVG(e.score) > 0 THEN 'Poor' " +
            "  ELSE 'Not Evaluated' " +
            "END, COUNT(DISTINCT e.staffId) " +
            "FROM Evaluation e " +
            "WHERE e.staffId IN (SELECT u.staffId FROM User u WHERE u.role = 'STAFF') " +
            "GROUP BY CASE " +
            "  WHEN AVG(e.score) >= 90 THEN 'Excellent' " +
            "  WHEN AVG(e.score) >= 75 THEN 'Good' " +
            "  WHEN AVG(e.score) >= 60 THEN 'Satisfactory' " +
            "  WHEN AVG(e.score) >= 40 THEN 'Needs Improvement' " +
            "  WHEN AVG(e.score) > 0 THEN 'Poor' " +
            "  ELSE 'Not Evaluated' " +
            "END")
    List<Object[]> countStaffByPerformanceLevel();

}