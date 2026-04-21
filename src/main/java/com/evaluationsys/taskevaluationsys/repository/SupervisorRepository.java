package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Branch;
import com.evaluationsys.taskevaluationsys.entity.Department;
import com.evaluationsys.taskevaluationsys.entity.Supervisor;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {

    // =========================
    // BASIC FETCH (WITH RELATIONS)
    // =========================
    @EntityGraph(attributePaths = {"user", "department", "branch"})
    Optional<Supervisor> findBySupervisorCode(String supervisorCode);

    Optional<Supervisor> findTopBySupervisorCodeStartingWithOrderBySupervisorCodeDesc(String prefix);

    // =========================
    // USER RELATION MAPPING
    // =========================
    Optional<Supervisor> findByUser(User user);

    List<Supervisor> findAllByUser(User user);

    Optional<Supervisor> findByUserAndBranchAndDepartment(
            User user,
            Branch branch,
            Department department
    );

    // =========================
    // FIND BY STAFF CODE/ID
    // =========================
    @Query("SELECT s FROM Supervisor s WHERE s.user.staffCode = :staffCode")
    Optional<Supervisor> findByUserStaffCode(@Param("staffCode") Long staffCode);

    @Query("SELECT s FROM Supervisor s WHERE s.user.staffId = :staffId")
    Optional<Supervisor> findByUserStaffId(@Param("staffId") Long staffId);

    // =========================
    // GET ALL SUPERVISOR CODES FOR A STAFF USER
    // =========================
    @Query("SELECT s.supervisorCode FROM Supervisor s WHERE s.user.staffCode = :staffCode")
    List<String> findSupervisorCodesByUserStaffCode(@Param("staffCode") Long staffCode);

    // =========================
    // CORE LOGIC YOU NEED (ROLE + DEPARTMENT)
    // =========================
    @Query("""
        SELECT s FROM Supervisor s
        WHERE s.user.role = :role
        AND s.department = :department
    """)
    Optional<Supervisor> findByUser_RoleAndUser_Department(
            @Param("role") Role role,
            @Param("department") Department department
    );

    // =========================
    // ✅ ADDED: COUNT METHOD
    // =========================
    long count();

    @Query("SELECT COUNT(s) FROM Supervisor s")
    long countAllSupervisors();

    // =========================
    // ✅ ADDED: FIND BY DEPARTMENT
    // =========================
    List<Supervisor> findByDepartment_DepartmentId(Long departmentId);

    @Query("SELECT s FROM Supervisor s WHERE s.department.departmentId = :deptId ORDER BY s.supervisorCode")
    List<Supervisor> findByDepartmentIdOrdered(@Param("deptId") Long deptId);

    @EntityGraph(attributePaths = {"user", "department", "branch"})
    @Query("SELECT s FROM Supervisor s WHERE s.department.departmentId = :deptId")
    List<Supervisor> findByDepartmentIdWithDetails(@Param("deptId") Long deptId);

    // =========================
    // ✅ ADDED: FIND BY BRANCH
    // =========================
    List<Supervisor> findByBranch_BranchId(Long branchId);

    @Query("SELECT s FROM Supervisor s WHERE s.branch.branchId = :branchId ORDER BY s.supervisorCode")
    List<Supervisor> findByBranchIdOrdered(@Param("branchId") Long branchId);

    // =========================
    // ✅ ADDED: FIND ALL WITH USER DETAILS
    // =========================
    @EntityGraph(attributePaths = {"user", "department", "branch"})
    @Query("SELECT s FROM Supervisor s ORDER BY s.supervisorCode")
    List<Supervisor> findAllWithDetails();

    @Query("SELECT s FROM Supervisor s JOIN FETCH s.user ORDER BY s.supervisorCode")
    List<Supervisor> findAllWithUser();

    // =========================
    // ✅ ADDED: STATISTICS
    // =========================
    @Query("SELECT s.department.departmentName, COUNT(s) FROM Supervisor s WHERE s.department IS NOT NULL GROUP BY s.department.departmentName")
    List<Object[]> countSupervisorsGroupByDepartment();

    @Query("SELECT s.branch.branchName, COUNT(s) FROM Supervisor s WHERE s.branch IS NOT NULL GROUP BY s.branch.branchName")
    List<Object[]> countSupervisorsGroupByBranch();

    @Query("SELECT s, COUNT(DISTINCT u.staffId) FROM Supervisor s " +
            "LEFT JOIN User u ON u.department.departmentId = s.department.departmentId AND u.role = 'STAFF' " +
            "GROUP BY s.supervisorId")
    List<Object[]> findSupervisorsWithStaffCount();

    // =========================
    // ✅ ADDED: SEARCH
    // =========================
    @Query("SELECT s FROM Supervisor s WHERE " +
            "LOWER(s.supervisorCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.user.otherName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Supervisor> searchSupervisors(@Param("query") String query);

    // =========================
    // ✅ ADDED: EXISTS CHECKS
    // =========================
    boolean existsBySupervisorCode(String supervisorCode);

    boolean existsByUser_StaffId(Long staffId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Supervisor s WHERE s.department.departmentId = :deptId AND s.user.staffId = :staffId")
    boolean existsByDepartmentAndStaffId(@Param("deptId") Long deptId, @Param("staffId") Long staffId);
}