package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================
    // PRIMARY IDENTIFIERS
    // =========================

    Optional<User> findByStaffCode(Long staffCode);
    Optional<User> findByStaffId(Long staffId);

    // Find by email
    Optional<User> findByEmail(String email);

    boolean existsByStaffCode(Long staffCode);
    boolean existsByStaffId(Long staffId);
    boolean existsByEmail(String email);

    // Find by staff code OR email (for password reset)
    @Query("SELECT u FROM User u WHERE CAST(u.staffCode AS string) = :query OR u.email = :query")
    Optional<User> findByStaffCodeOrEmail(@Param("query") String query);

    // =========================
    // ROLE-BASED FETCHING
    // =========================

    List<User> findByRole(Role role);
    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countAllByRole(@Param("role") Role role);

    // =========================
    // SEARCH
    // =========================

    Optional<User> findByFirstNameAndOtherName(String firstName, String otherName);

    List<User> findByFirstNameContainingIgnoreCaseOrOtherNameContainingIgnoreCase(
            String firstName, String otherName
    );

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.otherName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);

    // =========================
    // UPDATE ROLE
    // =========================

    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.staffCode = :staffCode")
    int updateRoleByStaffCode(@Param("staffCode") Long staffCode, @Param("role") Role role);

    // =========================
    // DEPARTMENT & BRANCH FILTERING - FIXED WITH CORRECT FIELD NAMES
    // =========================

    @Query("SELECT u FROM User u WHERE u.department.departmentId = :departmentId AND u.role = :role")
    List<User> findByDepartmentIdAndRole(@Param("departmentId") Long departmentId,
                                         @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.branch.branchId = :branchId AND u.role = :role")
    List<User> findByBranchIdAndRole(@Param("branchId") Long branchId,
                                     @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId AND u.role = 'STAFF'")
    List<User> findStaffByDepartmentId(@Param("deptId") Long deptId);

    @Query("SELECT u FROM User u WHERE u.branch.branchId = :branchId AND u.role = 'STAFF'")
    List<User> findStaffByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT u FROM User u WHERE u.role = 'STAFF'")
    List<User> findAllStaff();

    @Query("SELECT u FROM User u WHERE u.role = 'SUPERVISOR'")
    List<User> findAllSupervisors();

    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

    // =========================
    // FILTER BY ROLE, BRANCH, AND DEPARTMENT - FIXED METHODS
    // =========================

    // Find by role and branch (using branch.branchId)
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.branch.branchId = :branchId")
    List<User> findByRoleAndBranchId(@Param("role") Role role, @Param("branchId") Long branchId);

    // Find by role and department (using department.departmentId)
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.department.departmentId = :departmentId")
    List<User> findByRoleAndDepartmentId(@Param("role") Role role, @Param("departmentId") Long departmentId);

    // Find by role, branch, and department
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.branch.branchId = :branchId AND u.department.departmentId = :departmentId")
    List<User> findByRoleAndBranchIdAndDepartmentId(@Param("role") Role role,
                                                    @Param("branchId") Long branchId,
                                                    @Param("departmentId") Long departmentId);

    // =========================
    // STATISTICS
    // =========================

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'STAFF'")
    long countStaff();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'SUPERVISOR'")
    long countSupervisors();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    long countAdmins();

    @Query("SELECT u.department.departmentName, COUNT(u) FROM User u WHERE u.role = 'STAFF' AND u.department IS NOT NULL GROUP BY u.department.departmentName")
    List<Object[]> countStaffGroupByDepartment();

    @Query("SELECT u.branch.branchName, COUNT(u) FROM User u WHERE u.role = 'STAFF' AND u.branch IS NOT NULL GROUP BY u.branch.branchName")
    List<Object[]> countStaffGroupByBranch();
}