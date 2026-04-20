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

import java.util.List;
import java.util.Optional;

public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {

    // =========================
    // BASIC FETCH (WITH RELATIONS)
    // =========================
    @EntityGraph(attributePaths = {"user", "department", "department.branch"})
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
    // FIND BY STAFF CODE
    // =========================
    @Query("SELECT s FROM Supervisor s WHERE s.user.staffCode = :staffCode")
    Optional<Supervisor> findByUserStaffCode(@Param("staffCode") Long staffCode);

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
}