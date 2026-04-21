package com.evaluationsys.taskevaluationsys.service.supervisor;

import com.evaluationsys.taskevaluationsys.dtoresponse.StaffAssignmentGroupDTOResponse;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskAssignmentDTOResponse;
import com.evaluationsys.taskevaluationsys.dtoresponse.TaskDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupervisorTaskService {

    private static final Logger log = LoggerFactory.getLogger(SupervisorTaskService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository assignmentRepository;

    @Autowired
    private SupervisorRepository supervisorRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================
    // GET ALL SUPERVISOR RECORDS FOR USER
    // =========================
    private List<Supervisor> getAllSupervisorRecords(User user) {
        if (user == null || user.getStaffId() == null) {
            log.warn("User or staffId is null");
            return List.of();
        }

        List<Supervisor> records = supervisorRepository.findAll().stream()
                .filter(s -> s.getUser() != null)
                .filter(s -> s.getUser().getStaffId() != null)
                .filter(s -> s.getUser().getStaffId().equals(user.getStaffId()))
                .collect(Collectors.toList());

        log.info("✅ Found {} supervisor records for user: {}", records.size(), user.getStaffId());
        records.forEach(s -> log.info("   - Dept: {}, Branch: {}",
                s.getDepartment() != null ? s.getDepartment().getDepartmentName() : "NONE",
                s.getBranch() != null ? s.getBranch().getBranchName() : "NONE"));

        return records;
    }

    // =========================
    // GET ALL DEPARTMENT IDs SUPERVISOR IS ASSIGNED TO
    // =========================
    private Set<Long> getSupervisorDepartmentIds(User user) {
        List<Supervisor> records = getAllSupervisorRecords(user);
        Set<Long> deptIds = records.stream()
                .filter(s -> s.getDepartment() != null)
                .map(s -> s.getDepartment().getDepartmentId())
                .collect(Collectors.toSet());
        log.info("📁 Supervisor assigned to {} department(s): {}", deptIds.size(), deptIds);
        return deptIds;
    }

    // =========================
    // GET ALL STAFF IN SUPERVISOR'S DEPARTMENTS (ACROSS ALL BRANCHES)
    // =========================
    private List<User> getAllStaffInSupervisorDepartments(Set<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            log.warn("No department IDs provided");
            return List.of();
        }

        List<User> allStaff = userRepository.findAll().stream()
                .filter(u -> u.getDepartment() != null)
                .filter(u -> deptIds.contains(u.getDepartment().getDepartmentId()))
                .filter(u -> u.getRole() != null)
                .filter(u -> u.getRole() == Role.STAFF)
                .collect(Collectors.toList());

        log.info("👥 Found {} STAFF in supervisor's departments", allStaff.size());
        return allStaff;
    }

    // =========================
    // GET ALL ASSIGNMENTS FOR SUPERVISOR'S DEPARTMENTS
    // =========================
    private List<TaskAssignment> getAssignmentsForDepartments(Set<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            log.warn("No department IDs provided");
            return List.of();
        }

        List<TaskAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getAssignUser() != null)
                .filter(a -> a.getAssignUser().getDepartment() != null)
                .filter(a -> deptIds.contains(a.getAssignUser().getDepartment().getDepartmentId()))
                .collect(Collectors.toList());

        log.info("📋 Found {} assignments in supervisor's departments", assignments.size());
        return assignments;
    }

    // =========================================================
    // GROUPED ASSIGNMENTS BY STAFF - MAIN METHOD
    // =========================================================
    public List<StaffAssignmentGroupDTOResponse> getGroupedAssignmentsBySupervisor(User user) {
        log.info("═══════════════════════════════════════════════════");
        log.info("🚀 GETTING GROUPED ASSIGNMENTS FOR SUPERVISOR");
        log.info("═══════════════════════════════════════════════════");

        Set<Long> deptIds = getSupervisorDepartmentIds(user);
        if (deptIds.isEmpty()) {
            log.error("❌ Supervisor has NO departments assigned!");
            return List.of();
        }

        List<User> allStaff = getAllStaffInSupervisorDepartments(deptIds);
        if (allStaff.isEmpty()) {
            log.warn("⚠️ No STAFF found in supervisor's departments!");
            return List.of();
        }

        List<TaskAssignment> allAssignments = getAssignmentsForDepartments(deptIds);

        Map<Long, List<TaskAssignment>> assignmentsByStaffId = allAssignments.stream()
                .filter(a -> a.getAssignUser() != null)
                .collect(Collectors.groupingBy(a -> a.getAssignUser().getStaffId()));

        List<StaffAssignmentGroupDTOResponse> result = new ArrayList<>();

        for (User staff : allStaff) {
            String fullName = ((staff.getFirstName() == null ? "" : staff.getFirstName()) + " " +
                    (staff.getOtherName() == null ? "" : staff.getOtherName())).trim();

            String department = staff.getDepartment() != null ?
                    staff.getDepartment().getDepartmentName() : "N/A";
            String branch = staff.getBranch() != null ?
                    staff.getBranch().getBranchName() : "N/A";

            List<TaskAssignment> staffAssignments = assignmentsByStaffId.getOrDefault(staff.getStaffId(), new ArrayList<>());

            List<TaskAssignmentDTOResponse> assignmentDTOs = staffAssignments.stream()
                    .map(this::convertToTaskAssignmentDTOResponse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            result.add(new StaffAssignmentGroupDTOResponse(
                    staff.getStaffCode(), fullName, department, branch, assignmentDTOs));
        }

        result.sort(Comparator.comparing(StaffAssignmentGroupDTOResponse::getStaffName));
        log.info("✅ TOTAL STAFF GROUPS: {}", result.size());
        return result;
    }

    // =========================
    // GET TASK ASSIGNMENTS BY STATUS
    // =========================
    public List<TaskAssignmentDTOResponse> getAssignmentsByStatus(User user, TaskStatus status) {
        Set<Long> deptIds = getSupervisorDepartmentIds(user);
        if (deptIds.isEmpty()) return List.of();
        return getAssignmentsForDepartments(deptIds).stream()
                .filter(a -> a.getStatus() == status)
                .map(this::convertToTaskAssignmentDTOResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<TaskAssignmentDTOResponse> getPendingReviewAssignments(User user) {
        return getAssignmentsByStatus(user, TaskStatus.PENDING_REVIEW);
    }

    public List<TaskAssignmentDTOResponse> getInProgressAssignments(User user) {
        return getAssignmentsByStatus(user, TaskStatus.IN_PROGRESS);
    }

    public List<TaskAssignmentDTOResponse> getAllSupervisorAssignments(User user) {
        Set<Long> deptIds = getSupervisorDepartmentIds(user);
        if (deptIds.isEmpty()) return List.of();
        return getAssignmentsForDepartments(deptIds).stream()
                .map(this::convertToTaskAssignmentDTOResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================
    // GET TASKS BY STATUS - SHOW ALL ASSIGNMENTS
    // =========================
    public List<TaskDTOResponse> getTasksByStatus(User user, TaskStatus status) {
        Set<Long> deptIds = getSupervisorDepartmentIds(user);
        if (deptIds.isEmpty()) return List.of();
        return getAssignmentsForDepartments(deptIds).stream()
                .filter(a -> a.getStatus() == status)
                .map(this::convertAssignmentToTaskDTOResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<TaskDTOResponse> getPendingReviewTasks(User user) {
        return getTasksByStatus(user, TaskStatus.PENDING_REVIEW);
    }

    public List<TaskDTOResponse> getInProgressTasks(User user) {
        return getTasksByStatus(user, TaskStatus.IN_PROGRESS);
    }

    public List<TaskDTOResponse> getAllSupervisorTasks(User user) {
        Set<Long> deptIds = getSupervisorDepartmentIds(user);
        if (deptIds.isEmpty()) return List.of();
        return getAssignmentsForDepartments(deptIds).stream()
                .map(TaskAssignment::getTask)
                .filter(Objects::nonNull)
                .distinct()
                .map(this::convertToTaskDTOResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // ✅ APPROVE ASSIGNMENT - FIXED (Only approves this specific assignment)
    // =========================
    @Transactional
    public boolean approveAssignment(Long taskId, Long staffId, User user) {
        Optional<TaskAssignment> optionalAssignment =
                assignmentRepository.findByTaskIdAndStaffId(taskId, staffId);

        if (optionalAssignment.isEmpty()) {
            log.warn("Assignment not found for taskId: {} and staffId: {}", taskId, staffId);
            return false;
        }

        TaskAssignment assignment = optionalAssignment.get();

        if (assignment.getStatus() != TaskStatus.PENDING_REVIEW) {
            log.warn("Assignment {} is not PENDING_REVIEW, current status: {}",
                    assignment.getTaskAssignCode(), assignment.getStatus());
            return false;
        }

        if (!belongsToSupervisorDept(assignment.getTask(), user)) {
            log.warn("Task does not belong to supervisor's department");
            return false;
        }

        // ✅ ONLY update this specific assignment
        assignment.setStatus(TaskStatus.APPROVED);
        assignmentRepository.save(assignment);

        log.info("✅ Assignment {} approved for staff {}", assignment.getTaskAssignCode(), staffId);

        // ✅ Check if ALL assignments for this task are APPROVED
        // Only then update the task status
        List<TaskAssignment> allAssignments = assignmentRepository.findByTask_TaskId(taskId);
        boolean allApproved = allAssignments.stream()
                .allMatch(a -> a.getStatus() == TaskStatus.APPROVED);

        if (allApproved) {
            Task task = assignment.getTask();
            task.setTaskStatus(TaskStatus.APPROVED);
            taskRepository.save(task);
            log.info("📋 All assignments approved for task {}, task status updated to APPROVED", taskId);
        } else {
            log.info("⏳ Waiting for other assignments of task {} to be approved", taskId);
        }

        return true;
    }

    // =========================
    // ✅ REJECT ASSIGNMENT - FIXED (Only rejects this specific assignment)
    // =========================
    @Transactional
    public boolean rejectAssignment(Long taskId, Long staffId, User user, String reason) {
        Optional<TaskAssignment> optionalAssignment =
                assignmentRepository.findByTaskIdAndStaffId(taskId, staffId);

        if (optionalAssignment.isEmpty()) {
            log.warn("Assignment not found for taskId: {} and staffId: {}", taskId, staffId);
            return false;
        }

        TaskAssignment assignment = optionalAssignment.get();

        if (assignment.getStatus() != TaskStatus.PENDING_REVIEW) {
            log.warn("Assignment {} is not PENDING_REVIEW, current status: {}",
                    assignment.getTaskAssignCode(), assignment.getStatus());
            return false;
        }

        if (!belongsToSupervisorDept(assignment.getTask(), user)) {
            log.warn("Task does not belong to supervisor's department");
            return false;
        }

        // ✅ ONLY update this specific assignment
        assignment.setStatus(TaskStatus.REJECTED);
        // assignment.setRejectionReason(reason); // Uncomment if you have this field
        assignmentRepository.save(assignment);

        log.info("❌ Assignment {} rejected for staff {}. Reason: {}",
                assignment.getTaskAssignCode(), staffId, reason);

        // ✅ Task becomes IN_PROGRESS if any assignment is rejected or still active
        Task task = assignment.getTask();
        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);
        log.info("📋 Task {} status set to IN_PROGRESS due to rejection", taskId);

        return true;
    }

    // =========================
    // APPROVE TASK (Legacy - approves all assignments)
    // =========================
    @Transactional
    public boolean approveTask(Long taskId, User user) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;

        if (task.getTaskStatus() != TaskStatus.PENDING_REVIEW) return false;
        if (!belongsToSupervisorDept(task, user)) return false;

        task.setTaskStatus(TaskStatus.APPROVED);
        taskRepository.save(task);

        updateAssignmentStatusByTask(taskId, TaskStatus.APPROVED);
        return true;
    }

    // =========================
    // REJECT TASK (Legacy - rejects all assignments)
    // =========================
    @Transactional
    public boolean rejectTask(Long taskId, User user, String reason) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;

        if (task.getTaskStatus() != TaskStatus.PENDING_REVIEW) return false;
        if (!belongsToSupervisorDept(task, user)) return false;

        task.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        updateAssignmentStatusByTask(taskId, TaskStatus.IN_PROGRESS);
        return true;
    }

    // =========================
    // SECURITY CHECK
    // =========================
    private boolean belongsToSupervisorDept(Task task, User user) {
        Set<Long> deptIds = getSupervisorDepartmentIds(user);
        if (deptIds.isEmpty()) return false;

        return assignmentRepository.findByTask_TaskId(task.getTaskId()).stream()
                .anyMatch(a ->
                        a.getAssignUser() != null &&
                                a.getAssignUser().getDepartment() != null &&
                                deptIds.contains(a.getAssignUser().getDepartment().getDepartmentId())
                );
    }

    // =========================
    // UPDATE STATUS
    // =========================
    private void updateAssignmentStatusByTask(Long taskId, TaskStatus status) {
        List<TaskAssignment> assignments = assignmentRepository.findByTask_TaskId(taskId);
        assignments.forEach(a -> {
            a.setStatus(status);
            assignmentRepository.save(a);
        });
    }

    // =========================
    // STATISTICS
    // =========================
    public SupervisorStatistics getSupervisorStatistics(User user) {
        Set<Long> deptIds = getSupervisorDepartmentIds(user);
        if (deptIds.isEmpty()) return new SupervisorStatistics();

        List<TaskAssignment> assignments = getAssignmentsForDepartments(deptIds);

        SupervisorStatistics stats = new SupervisorStatistics();
        stats.setTotalTasks(assignments.size());
        stats.setPendingReviewTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.PENDING_REVIEW).count());
        stats.setInProgressTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.IN_PROGRESS).count());
        stats.setApprovedTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.APPROVED).count());
        stats.setInitiatedTasks((int) assignments.stream()
                .filter(a -> a.getStatus() == TaskStatus.ASSIGNED
                        || a.getStatus() == TaskStatus.INITIATED)
                .count());

        int reviewed = stats.getPendingReviewTasks() + stats.getApprovedTasks();
        if (reviewed > 0) {
            stats.setApprovalRate((stats.getApprovedTasks() * 100.0) / reviewed);
        }
        return stats;
    }

    // =========================
    // DTO MAPPERS
    // =========================
    private TaskAssignmentDTOResponse convertToTaskAssignmentDTOResponse(TaskAssignment assignment) {
        if (assignment == null) return null;

        Task task = assignment.getTask();
        User staff = assignment.getAssignUser();
        if (task == null || staff == null) return null;

        return new TaskAssignmentDTOResponse(
                assignment.getTaskAssignCode(),
                task.getTaskId(),
                task.getDescription(),
                task.getDeadline(),
                staff.getStaffId(),
                staff.getFirstName(),
                staff.getOtherName(),
                staff.getDepartment() != null ? staff.getDepartment().getDepartmentName() : null,
                staff.getBranch() != null ? staff.getBranch().getBranchName() : null,
                assignment.getAssignedAt(),
                assignment.getStatus() != null ? assignment.getStatus().name() : null
        );
    }

    private TaskDTOResponse convertAssignmentToTaskDTOResponse(TaskAssignment assignment) {
        if (assignment == null) return null;

        Task task = assignment.getTask();
        User staff = assignment.getAssignUser();
        if (task == null) return null;

        TaskDTOResponse r = new TaskDTOResponse();
        r.setTaskId(task.getTaskId());
        r.setTaskCode(task.getTaskCode());
        r.setDescription(task.getDescription());
        r.setTaskStatus(assignment.getStatus());
        r.setDeadline(task.getDeadline());
        r.setQuarter(task.getQuarter());
        r.setYear(task.getYear());
        r.setCreatedAt(task.getCreatedAt());
        r.setUpdatedAt(task.getUpdatedAt());

        if (staff != null) {
            String name = ((staff.getFirstName() == null ? "" : staff.getFirstName()) + " " +
                    (staff.getOtherName() == null ? "" : staff.getOtherName())).trim();
            r.setCreatedByStaffCode(staff.getStaffCode());
            r.setCreatedByName(name);
        }
        return r;
    }

    private TaskDTOResponse convertToTaskDTOResponse(Task task) {
        if (task == null) return null;

        TaskDTOResponse r = new TaskDTOResponse();
        r.setTaskId(task.getTaskId());
        r.setTaskCode(task.getTaskCode());
        r.setDescription(task.getDescription());
        r.setTaskStatus(task.getTaskStatus());
        r.setDeadline(task.getDeadline());
        r.setQuarter(task.getQuarter());
        r.setYear(task.getYear());
        r.setCreatedAt(task.getCreatedAt());
        r.setUpdatedAt(task.getUpdatedAt());

        List<TaskAssignment> assignments = assignmentRepository.findByTask_TaskId(task.getTaskId());
        if (!assignments.isEmpty() && assignments.get(0).getAssignUser() != null) {
            User staff = assignments.get(0).getAssignUser();
            String name = ((staff.getFirstName() == null ? "" : staff.getFirstName()) + " " +
                    (staff.getOtherName() == null ? "" : staff.getOtherName())).trim();
            r.setCreatedByStaffCode(staff.getStaffCode());
            r.setCreatedByName(name);
        }
        return r;
    }

    public Supervisor getSupervisorDirect(User user) {
        List<Supervisor> records = getAllSupervisorRecords(user);
        return records.isEmpty() ? null : records.get(0);
    }
}