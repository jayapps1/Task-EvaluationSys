package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // =========================
    // BASIC QUERIES
    // =========================

    Optional<Department> findByDepartmentCode(String departmentCode);

    Optional<Department> findTopByDepartmentCodeStartingWithOrderByDepartmentCodeDesc(String prefix);

    Optional<Department> findByDepartmentName(String departmentName);

    List<Department> findByDepartmentNameContainingIgnoreCase(String departmentName);

    boolean existsByDepartmentName(String departmentName);

    boolean existsByDepartmentCode(String departmentCode);

    // ✅ ADDED: Count method
    long count();

    // ✅ ADDED: Find all ordered by name
    @Query("SELECT d FROM Department d ORDER BY d.departmentName ASC")
    List<Department> findAllOrdered();

    // ✅ ADDED: Find all ordered by code
    @Query("SELECT d FROM Department d ORDER BY d.departmentCode ASC")
    List<Department> findAllOrderedByCode();

    // ✅ ADDED: Search departments by name or code
    @Query("SELECT d FROM Department d WHERE LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(d.departmentCode) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Department> searchDepartments(@Param("query") String query);

    // =========================
    // STATISTICS
    // =========================

    @Query("SELECT COUNT(d) FROM Department d")
    long countAllDepartments();

    @Query("SELECT d.departmentName, COUNT(u) FROM Department d " +
            "LEFT JOIN User u ON u.department.departmentId = d.departmentId AND u.role = 'STAFF' " +
            "GROUP BY d.departmentId, d.departmentName")
    List<Object[]> getStaffCountByDepartment();

    @Query("SELECT d.departmentName, COUNT(s) FROM Department d " +
            "LEFT JOIN Supervisor s ON s.department.departmentId = d.departmentId " +
            "GROUP BY d.departmentId, d.departmentName")
    List<Object[]> getSupervisorCountByDepartment();

    @Query("SELECT d.departmentName, " +
            "COUNT(DISTINCT u.staffId) as staffCount, " +
            "COUNT(DISTINCT s.supervisorId) as supervisorCount " +
            "FROM Department d " +
            "LEFT JOIN User u ON u.department.departmentId = d.departmentId AND u.role = 'STAFF' " +
            "LEFT JOIN Supervisor s ON s.department.departmentId = d.departmentId " +
            "GROUP BY d.departmentId, d.departmentName")
    List<Object[]> getDepartmentStatistics();
}