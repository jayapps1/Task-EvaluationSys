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

    Optional<User> findByStaffCode(Long staffCode);  // legacy
    Optional<User> findByStaffId(Long staffId);      // new

    boolean existsByStaffCode(Long staffCode);       // legacy
    boolean existsByStaffId(Long staffId);           // new
    boolean existsByEmail(String email);

    // =========================
    // ROLE-BASED FETCHING
    // =========================

    List<User> findByRole(Role role);

    // ✅ ADDED: Count by role
    long countByRole(Role role);

    // ✅ ADDED: Find all by role with JPQL
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") Role role);

    // ✅ ADDED: Count all by role with JPQL
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countAllByRole(@Param("role") Role role);

    // =========================
    // SEARCH
    // =========================

    Optional<User> findByFirstNameAndOtherName(String firstName, String otherName);

    List<User> findByFirstNameContainingIgnoreCaseOrOtherNameContainingIgnoreCase(
            String firstName, String otherName
    );

    // ✅ ADDED: Search by name or email
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
    // DEPARTMENT & BRANCH FILTERING
    // =========================

    @Query("SELECT u FROM User u WHERE u.department.departmentId = :departmentId AND u.role = :role")
    List<User> findByDepartmentIdAndRole(@Param("departmentId") Long departmentId,
                                         @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.branch.branchId = :branchId AND u.role = :role")
    List<User> findByBranchIdAndRole(@Param("branchId") Long branchId,
                                     @Param("role") Role role);

    // ✅ ADDED: Find staff by department only
    @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId AND u.role = 'STAFF'")
    List<User> findStaffByDepartmentId(@Param("deptId") Long deptId);

    // ✅ ADDED: Find staff by branch only
    @Query("SELECT u FROM User u WHERE u.branch.branchId = :branchId AND u.role = 'STAFF'")
    List<User> findStaffByBranchId(@Param("branchId") Long branchId);

    // ✅ ADDED: Find all staff with optional filters
    @Query("SELECT u FROM User u WHERE u.role = 'STAFF'")
    List<User> findAllStaff();

    // ✅ ADDED: Find all supervisors
    @Query("SELECT u FROM User u WHERE u.role = 'SUPERVISOR'")
    List<User> findAllSupervisors();

    // ✅ ADDED: Find all admins
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

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