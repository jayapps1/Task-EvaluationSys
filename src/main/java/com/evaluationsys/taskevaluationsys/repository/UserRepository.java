package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    // =========================
    // SEARCH
    // =========================

    Optional<User> findByFirstNameAndOtherName(String firstName, String otherName);

    List<User> findByFirstNameContainingIgnoreCaseOrOtherNameContainingIgnoreCase(
            String firstName, String otherName
    );

    // =========================
    // UPDATE ROLE
    // =========================

    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.staffCode = :staffCode")
    int updateRoleByStaffCode(@Param("staffCode") Long staffCode, @Param("role") Role role);

    // =========================
    // FILTERING (OPTIONAL BUT GOOD)
    // =========================

    @Query("SELECT u FROM User u WHERE u.department.departmentId = :departmentId AND u.role = :role")
    List<User> findByDepartmentIdAndRole(@Param("departmentId") Long departmentId,
                                         @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.branch.branchId = :branchId AND u.role = :role")
    List<User> findByBranchIdAndRole(@Param("branchId") Long branchId,
                                     @Param("role") Role role);
}