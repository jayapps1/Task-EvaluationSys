package com.evaluationsys.taskevaluationsys.repository;

import com.evaluationsys.taskevaluationsys.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    // =========================
    // BASIC QUERIES
    // =========================

    Optional<Branch> findByBranchCode(String branchCode);

    Optional<Branch> findTopByBranchCodeStartingWithOrderByBranchCodeDesc(String prefix);

    Optional<Branch> findByBranchName(String branchName);

    List<Branch> findByBranchNameContainingIgnoreCase(String branchName);

    boolean existsByBranchName(String branchName);

    boolean existsByBranchCode(String branchCode);

    // ✅ ADDED: Count method
    long count();

    // ✅ ADDED: Find all ordered by name
    @Query("SELECT b FROM Branch b ORDER BY b.branchName ASC")
    List<Branch> findAllOrdered();

    // ✅ ADDED: Find all ordered by code
    @Query("SELECT b FROM Branch b ORDER BY b.branchCode ASC")
    List<Branch> findAllOrderedByCode();

    // ✅ ADDED: Search branches
    @Query("SELECT b FROM Branch b WHERE LOWER(b.branchName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.branchCode) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Branch> searchBranches(@Param("query") String query);

    // =========================
    // STATISTICS
    // =========================

    @Query("SELECT COUNT(b) FROM Branch b")
    long countAllBranches();

    @Query("SELECT b.branchName, COUNT(u) FROM Branch b LEFT JOIN User u ON u.branch.branchId = b.branchId AND u.role = 'STAFF' GROUP BY b.branchName")
    List<Object[]> getStaffCountByBranch();
}