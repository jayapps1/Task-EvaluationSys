package com.evaluationsys.taskevaluationsys.service.admin;

import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupervisorRepository supervisorRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository assignmentRepository;

    // =========================
    // DASHBOARD STATISTICS - FIXED
    // =========================
    public AdminStatistics getDashboardStatistics() {
        AdminStatistics stats = new AdminStatistics();

        // User counts
        stats.setTotalStaff(userRepository.countByRole(Role.STAFF));

        // Count unique supervisors (by user staffId)
        long uniqueSupervisors = supervisorRepository.findAll().stream()
                .map(s -> s.getUser() != null ? s.getUser().getStaffId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        stats.setTotalSupervisors(uniqueSupervisors);

        stats.setTotalBranches(branchRepository.count());
        stats.setTotalDepartments(departmentRepository.count());

        // Task counts from TaskAssignment (each assignment = one task for a staff)
        List<TaskAssignment> allAssignments = assignmentRepository.findAll();
        stats.setTotalTasks(allAssignments.size());

        stats.setPendingTasks((int) allAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.ASSIGNED).count());

        stats.setInProgressTasks((int) allAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.INITIATED ||
                        a.getStatus() == TaskStatus.IN_PROGRESS).count());

        stats.setPendingReviewTasks((int) allAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.PENDING_REVIEW).count());

        stats.setPendingApprovalTasks((int) allAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.PENDING_APPROVAL).count());

        stats.setApprovedTasks((int) allAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.APPROVED).count());

        stats.setRejectedTasks((int) allAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.REJECTED).count());

        stats.setCompletedTasks((int) allAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.COMPLETED).count());

        return stats;
    }

    // =========================
    // FILTERED STATISTICS
    // =========================
    public AdminStatistics getFilteredStatistics(Long branchId, Long deptId, String quarter) {
        AdminStatistics stats = new AdminStatistics();

        // User counts (total counts don't change with task filters)
        stats.setTotalStaff(userRepository.countByRole(Role.STAFF));

        long uniqueSupervisors = supervisorRepository.findAll().stream()
                .map(s -> s.getUser() != null ? s.getUser().getStaffId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        stats.setTotalSupervisors(uniqueSupervisors);

        stats.setTotalBranches(branchRepository.count());
        stats.setTotalDepartments(departmentRepository.count());

        // Get filtered assignments
        List<TaskAssignment> filteredAssignments = getFilteredAssignments(branchId, deptId, quarter);

        stats.setTotalTasks(filteredAssignments.size());
        stats.setPendingTasks((int) filteredAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.ASSIGNED).count());
        stats.setInProgressTasks((int) filteredAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.INITIATED ||
                        a.getStatus() == TaskStatus.IN_PROGRESS).count());
        stats.setPendingReviewTasks((int) filteredAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.PENDING_REVIEW).count());
        stats.setPendingApprovalTasks((int) filteredAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.PENDING_APPROVAL).count());
        stats.setApprovedTasks((int) filteredAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.APPROVED).count());
        stats.setRejectedTasks((int) filteredAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.REJECTED).count());
        stats.setCompletedTasks((int) filteredAssignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.COMPLETED).count());

        return stats;
    }

    // =========================
    // FILTERED TASK COUNTS BY STATUS
    // =========================
    public Map<String, Long> getFilteredTaskCountsByStatus(Long branchId, Long deptId, String quarter) {
        List<TaskAssignment> filteredAssignments = getFilteredAssignments(branchId, deptId, quarter);

        return filteredAssignments.stream()
                .filter(a -> a.getStatus() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        Collectors.counting()
                ));
    }

    // =========================
    // HELPER: GET FILTERED ASSIGNMENTS
    // =========================
    private List<TaskAssignment> getFilteredAssignments(Long branchId, Long deptId, String quarter) {
        return assignmentRepository.findAll().stream()
                .filter(a -> {
                    User user = a.getAssignUser();
                    if (user == null) return false;

                    // Filter by branch
                    if (branchId != null && (user.getBranch() == null ||
                            !user.getBranch().getBranchId().equals(branchId))) {
                        return false;
                    }
                    // Filter by department
                    if (deptId != null && (user.getDepartment() == null ||
                            !user.getDepartment().getDepartmentId().equals(deptId))) {
                        return false;
                    }
                    // Filter by quarter
                    if (quarter != null && !"ALL".equals(quarter)) {
                        Task task = a.getTask();
                        if (task == null || task.getQuarter() == null ||
                                !task.getQuarter().name().equals(quarter)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ DEPARTMENT PERFORMANCE - WITH BRANCH NAME & IN PROGRESS
    // =========================
    public List<Map<String, Object>> getDepartmentPerformance() {
        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> performance = new ArrayList<>();

        for (Department dept : departments) {
            // Get branch name from staff in this department
            String branchName = "N/A";
            Optional<User> staffInDept = userRepository.findByRole(Role.STAFF).stream()
                    .filter(u -> u.getDepartment() != null &&
                            u.getDepartment().getDepartmentId().equals(dept.getDepartmentId()) &&
                            u.getBranch() != null)
                    .findFirst();
            if (staffInDept.isPresent()) {
                branchName = staffInDept.get().getBranch().getBranchName();
            }

            List<TaskAssignment> assignments = assignmentRepository.findAll().stream()
                    .filter(a -> a.getAssignUser() != null &&
                            a.getAssignUser().getDepartment() != null &&
                            a.getAssignUser().getDepartment().getDepartmentId().equals(dept.getDepartmentId()))
                    .collect(Collectors.toList());

            long total = assignments.size();
            long completed = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.APPROVED)
                    .count();
            long inProgress = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.INITIATED ||
                            a.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("departmentId", dept.getDepartmentId());
            stats.put("departmentName", dept.getDepartmentName());
            stats.put("branchName", branchName);
            stats.put("totalTasks", total);
            stats.put("completedTasks", completed);
            stats.put("inProgressTasks", inProgress);
            stats.put("completionRate", total > 0 ? (completed * 100.0 / total) : 0);

            performance.add(stats);
        }

        return performance.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("completionRate"), (Double) a.get("completionRate")))
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ FILTERED DEPARTMENT PERFORMANCE - WITH IN PROGRESS
    // =========================
    public List<Map<String, Object>> getDepartmentPerformanceFiltered(Long branchId, Long deptId, String quarter) {
        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> performance = new ArrayList<>();

        for (Department dept : departments) {
            // Get branch name from staff in this department
            String branchName = "N/A";
            Optional<User> staffInDept = userRepository.findByRole(Role.STAFF).stream()
                    .filter(u -> u.getDepartment() != null &&
                            u.getDepartment().getDepartmentId().equals(dept.getDepartmentId()) &&
                            u.getBranch() != null)
                    .findFirst();
            if (staffInDept.isPresent()) {
                branchName = staffInDept.get().getBranch().getBranchName();
            }

            List<TaskAssignment> assignments = getFilteredAssignments(branchId, deptId, quarter).stream()
                    .filter(a -> a.getAssignUser() != null &&
                            a.getAssignUser().getDepartment() != null &&
                            a.getAssignUser().getDepartment().getDepartmentId().equals(dept.getDepartmentId()))
                    .collect(Collectors.toList());

            long total = assignments.size();
            long completed = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.APPROVED)
                    .count();
            long inProgress = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.INITIATED ||
                            a.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("departmentId", dept.getDepartmentId());
            stats.put("departmentName", dept.getDepartmentName());
            stats.put("branchName", branchName);
            stats.put("totalTasks", total);
            stats.put("completedTasks", completed);
            stats.put("inProgressTasks", inProgress);
            stats.put("completionRate", total > 0 ? (completed * 100.0 / total) : 0);

            performance.add(stats);
        }

        return performance.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("completionRate"), (Double) a.get("completionRate")))
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ BRANCH PERFORMANCE - WITH IN PROGRESS & PENDING
    // =========================
    public List<Map<String, Object>> getBranchPerformance() {
        List<Branch> branches = branchRepository.findAll();
        List<Map<String, Object>> performance = new ArrayList<>();

        for (Branch branch : branches) {
            List<TaskAssignment> assignments = assignmentRepository.findAll().stream()
                    .filter(a -> a.getAssignUser() != null &&
                            a.getAssignUser().getBranch() != null &&
                            a.getAssignUser().getBranch().getBranchId().equals(branch.getBranchId()))
                    .collect(Collectors.toList());

            long total = assignments.size();
            long completed = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.APPROVED)
                    .count();
            long inProgress = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.INITIATED ||
                            a.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();
            long pending = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.ASSIGNED)
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("branchId", branch.getBranchId());
            stats.put("branchName", branch.getBranchName());
            stats.put("totalTasks", total);
            stats.put("completedTasks", completed);
            stats.put("inProgressTasks", inProgress);
            stats.put("pendingTasks", pending);
            stats.put("completionRate", total > 0 ? (completed * 100.0 / total) : 0);

            performance.add(stats);
        }

        return performance.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("completionRate"), (Double) a.get("completionRate")))
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ FILTERED BRANCH PERFORMANCE - WITH IN PROGRESS & PENDING
    // =========================
    public List<Map<String, Object>> getBranchPerformanceFiltered(Long branchId, Long deptId, String quarter) {
        List<Branch> branches = branchRepository.findAll();
        List<Map<String, Object>> performance = new ArrayList<>();

        for (Branch branch : branches) {
            List<TaskAssignment> assignments = getFilteredAssignments(branchId, deptId, quarter).stream()
                    .filter(a -> a.getAssignUser() != null &&
                            a.getAssignUser().getBranch() != null &&
                            a.getAssignUser().getBranch().getBranchId().equals(branch.getBranchId()))
                    .collect(Collectors.toList());

            long total = assignments.size();
            long completed = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.APPROVED)
                    .count();
            long inProgress = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.INITIATED ||
                            a.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();
            long pending = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.ASSIGNED)
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("branchId", branch.getBranchId());
            stats.put("branchName", branch.getBranchName());
            stats.put("totalTasks", total);
            stats.put("completedTasks", completed);
            stats.put("inProgressTasks", inProgress);
            stats.put("pendingTasks", pending);
            stats.put("completionRate", total > 0 ? (completed * 100.0 / total) : 0);

            performance.add(stats);
        }

        return performance.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("completionRate"), (Double) a.get("completionRate")))
                .collect(Collectors.toList());
    }

    // =========================
    // STAFF COUNTS BY BRANCH
    // =========================
    public Map<String, Long> getStaffCountByBranch() {
        List<User> staff = userRepository.findByRole(Role.STAFF);
        return staff.stream()
                .filter(u -> u.getBranch() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getBranch().getBranchName(),
                        Collectors.counting()
                ));
    }

    // =========================
    // FILTERED STAFF COUNTS BY BRANCH
    // =========================
    public Map<String, Long> getStaffCountByBranchFiltered(Long branchId, Long deptId) {
        List<User> staff = userRepository.findByRole(Role.STAFF).stream()
                .filter(u -> {
                    if (branchId != null && (u.getBranch() == null ||
                            !u.getBranch().getBranchId().equals(branchId))) {
                        return false;
                    }
                    if (deptId != null && (u.getDepartment() == null ||
                            !u.getDepartment().getDepartmentId().equals(deptId))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return staff.stream()
                .filter(u -> u.getBranch() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getBranch().getBranchName(),
                        Collectors.counting()
                ));
    }

    // =========================
    // STAFF COUNTS BY DEPARTMENT
    // =========================
    public Map<String, Long> getStaffCountByDepartment() {
        List<User> staff = userRepository.findByRole(Role.STAFF);
        return staff.stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDepartment().getDepartmentName(),
                        Collectors.counting()
                ));
    }

    // =========================
    // FILTERED STAFF COUNTS BY DEPARTMENT
    // =========================
    public Map<String, Long> getStaffCountByDepartmentFiltered(Long branchId, Long deptId) {
        List<User> staff = userRepository.findByRole(Role.STAFF).stream()
                .filter(u -> {
                    if (branchId != null && (u.getBranch() == null ||
                            !u.getBranch().getBranchId().equals(branchId))) {
                        return false;
                    }
                    if (deptId != null && (u.getDepartment() == null ||
                            !u.getDepartment().getDepartmentId().equals(deptId))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return staff.stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDepartment().getDepartmentName(),
                        Collectors.counting()
                ));
    }

    // =========================
    // SUPERVISOR COUNTS BY DEPARTMENT (Unique supervisors)
    // =========================
    public Map<String, Long> getSupervisorCountByDepartment() {
        List<Supervisor> supervisors = supervisorRepository.findAll();

        return supervisors.stream()
                .filter(s -> s.getDepartment() != null && s.getUser() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getDepartment().getDepartmentName(),
                        Collectors.collectingAndThen(
                                Collectors.mapping(s -> s.getUser().getStaffId(), Collectors.toSet()),
                                set -> (long) set.size()
                        )
                ));
    }

    // =========================
    // FILTERED SUPERVISOR COUNTS BY DEPARTMENT
    // =========================
    public Map<String, Long> getSupervisorCountByDepartmentFiltered(Long deptId) {
        List<Supervisor> supervisors = supervisorRepository.findAll().stream()
                .filter(s -> {
                    if (deptId != null && (s.getDepartment() == null ||
                            !s.getDepartment().getDepartmentId().equals(deptId))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return supervisors.stream()
                .filter(s -> s.getDepartment() != null && s.getUser() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getDepartment().getDepartmentName(),
                        Collectors.collectingAndThen(
                                Collectors.mapping(s -> s.getUser().getStaffId(), Collectors.toSet()),
                                set -> (long) set.size()
                        )
                ));
    }

    // =========================
    // GET ALL STAFF
    // =========================
    public List<User> getAllStaff() {
        return userRepository.findByRole(Role.STAFF);
    }

    // =========================
    // GET FILTERED STAFF
    // =========================
    public List<User> getFilteredStaff(Long branchId, Long deptId) {
        return userRepository.findByRole(Role.STAFF).stream()
                .filter(u -> {
                    if (branchId != null && (u.getBranch() == null ||
                            !u.getBranch().getBranchId().equals(branchId))) {
                        return false;
                    }
                    if (deptId != null && (u.getDepartment() == null ||
                            !u.getDepartment().getDepartmentId().equals(deptId))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    // =========================
    // GET STAFF BY BRANCH
    // =========================
    public List<User> getStaffByBranch(Long branchId) {
        return userRepository.findByRole(Role.STAFF).stream()
                .filter(u -> u.getBranch() != null && u.getBranch().getBranchId().equals(branchId))
                .collect(Collectors.toList());
    }

    // =========================
    // GET STAFF BY DEPARTMENT
    // =========================
    public List<User> getStaffByDepartment(Long deptId) {
        return userRepository.findByRole(Role.STAFF).stream()
                .filter(u -> u.getDepartment() != null && u.getDepartment().getDepartmentId().equals(deptId))
                .collect(Collectors.toList());
    }

    // =========================
    // GET SUPERVISORS BY DEPARTMENT
    // =========================
    public List<Supervisor> getSupervisorsByDepartment(Long deptId) {
        return supervisorRepository.findAll().stream()
                .filter(s -> s.getDepartment() != null && s.getDepartment().getDepartmentId().equals(deptId))
                .collect(Collectors.toList());
    }

    // =========================
    // GET ALL BRANCHES
    // =========================
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    // =========================
    // GET ALL DEPARTMENTS
    // =========================
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // =========================
    // RECENT ASSIGNMENTS
    // =========================
    public List<TaskAssignmentDTOResponse> getRecentAssignments(int limit) {
        return assignmentRepository.findLatestAssignments().stream()
                .limit(limit)
                .map(this::convertToAssignmentResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // FILTERED RECENT ASSIGNMENTS
    // =========================
    public List<TaskAssignmentDTOResponse> getRecentAssignmentsFiltered(int limit, Long branchId, Long deptId, String quarter) {
        return assignmentRepository.findLatestAssignments().stream()
                .filter(a -> {
                    User user = a.getAssignUser();
                    if (user == null) return false;

                    if (branchId != null && (user.getBranch() == null ||
                            !user.getBranch().getBranchId().equals(branchId))) {
                        return false;
                    }
                    if (deptId != null && (user.getDepartment() == null ||
                            !user.getDepartment().getDepartmentId().equals(deptId))) {
                        return false;
                    }
                    if (quarter != null && !"ALL".equals(quarter)) {
                        Task task = a.getTask();
                        if (task == null || task.getQuarter() == null ||
                                !task.getQuarter().name().equals(quarter)) {
                            return false;
                        }
                    }
                    return true;
                })
                .limit(limit)
                .map(this::convertToAssignmentResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // UPDATE STAFF INFO
    // =========================
    public boolean updateStaffInfo(Long staffId, Long branchId, Long departmentId) {
        Optional<User> staffOpt = userRepository.findById(staffId);
        if (staffOpt.isEmpty()) return false;

        User staff = staffOpt.get();
        if (branchId != null) {
            branchRepository.findById(branchId).ifPresent(staff::setBranch);
        }
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(staff::setDepartment);
        }
        userRepository.save(staff);
        return true;
    }

    // =========================
    // TASK COUNTS BY STATUS
    // =========================
    public Map<String, Long> getTaskCountsByStatus() {
        return assignmentRepository.findAll().stream()
                .filter(a -> a.getStatus() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        Collectors.counting()
                ));
    }

    // =========================
    // ✅ GET ALL ASSIGNMENTS (for export)
    // =========================
    public List<TaskAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    // =========================
    // ✅ GET ALL SUPERVISORS (for export)
    // =========================
    public List<Supervisor> getAllSupervisors() {
        return supervisorRepository.findAll();
    }

    // =========================
    // MONTHLY COMPLETION STATS
    // =========================
    public Map<String, Long> getMonthlyCompletionStats() {
        return assignmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == TaskStatus.APPROVED)
                .filter(a -> a.getAssignedAt() != null)
                .collect(Collectors.groupingBy(
                        a -> String.format("%02d", a.getAssignedAt().getMonthValue()),
                        Collectors.counting()
                ));
    }

    // =========================
    // ✅ STAFF PERFORMANCE STATS - WITH IN PROGRESS
    // =========================
    public List<Map<String, Object>> getStaffPerformanceStats() {
        List<User> staff = userRepository.findByRole(Role.STAFF);
        List<Map<String, Object>> performance = new ArrayList<>();

        for (User s : staff) {
            List<TaskAssignment> assignments = assignmentRepository.findByAssignUser_StaffId(s.getStaffId());
            long completed = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.APPROVED).count();
            long inProgress = assignments.stream()
                    .filter(a -> a.getStatus() == TaskStatus.INITIATED ||
                            a.getStatus() == TaskStatus.IN_PROGRESS).count();
            long total = assignments.size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("staffId", s.getStaffId());
            stats.put("staffName", s.getFirstName() + " " + s.getOtherName());
            stats.put("staffCode", s.getStaffCode());
            stats.put("branch", s.getBranch() != null ? s.getBranch().getBranchName() : "N/A");
            stats.put("department", s.getDepartment() != null ? s.getDepartment().getDepartmentName() : "N/A");
            stats.put("totalTasks", total);
            stats.put("completedTasks", completed);
            stats.put("inProgressTasks", inProgress);
            stats.put("completionRate", total > 0 ? (completed * 100.0 / total) : 0);

            performance.add(stats);
        }

        return performance.stream()
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("completionRate"),
                        (Double) a.get("completionRate")))
                .collect(Collectors.toList());
    }

    // =========================
    // DTO CONVERTER
    // =========================
    private TaskAssignmentDTOResponse convertToAssignmentResponse(TaskAssignment a) {
        if (a == null) return null;

        Task task = a.getTask();
        User user = a.getAssignUser();

        if (task == null || user == null) return null;

        String departmentName = (user.getDepartment() != null)
                ? user.getDepartment().getDepartmentName() : "N/A";
        String branchName = (user.getBranch() != null)
                ? user.getBranch().getBranchName() : "N/A";

        return new TaskAssignmentDTOResponse(
                a.getTaskAssignCode(),
                task.getTaskId(),
                task.getDescription(),
                task.getDeadline(),
                user.getStaffId(),
                user.getFirstName(),
                user.getOtherName(),
                departmentName,
                branchName,
                a.getAssignedAt(),
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }
}