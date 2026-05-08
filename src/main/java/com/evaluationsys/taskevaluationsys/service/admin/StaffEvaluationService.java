package com.evaluationsys.taskevaluationsys.service.admin;

import com.evaluationsys.taskevaluationsys.dto.EvaluationDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.EvaluationDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.entity.enums.TaskStatus;
import com.evaluationsys.taskevaluationsys.repository.*;
import com.evaluationsys.taskevaluationsys.service.EvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StaffEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(StaffEvaluationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SupervisorRepository supervisorRepository;

    // =========================
    // SUMMARY METHODS
    // =========================

    public List<Map<String, Object>> getAllStaffEvaluationSummary() {
        List<User> staffMembers = userRepository.findByRole(Role.STAFF);
        log.info("Total staff members: {}", staffMembers.size());
        return staffMembers.stream()
                .map(this::getStaffEvaluationSummary)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getFilteredStaffEvaluationSummary(Long branchId, Long departmentId) {
        List<User> staffMembers;
        if (branchId != null && departmentId != null) {
            staffMembers = userRepository.findByRoleAndBranchIdAndDepartmentId(Role.STAFF, branchId, departmentId);
        } else if (branchId != null) {
            staffMembers = userRepository.findByRoleAndBranchId(Role.STAFF, branchId);
        } else if (departmentId != null) {
            staffMembers = userRepository.findByRoleAndDepartmentId(Role.STAFF, departmentId);
        } else {
            staffMembers = userRepository.findByRole(Role.STAFF);
        }
        log.info("Filtered staff members: {}", staffMembers.size());
        return staffMembers.stream()
                .map(this::getStaffEvaluationSummary)
                .collect(Collectors.toList());
    }

    /**
     * Evaluation summary for a single staff member – COMPLETELY ISOLATED using staff_id.
     * This ensures evaluations from one staff never affect another staff's data.
     */
    public Map<String, Object> getStaffEvaluationSummary(User staff) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("staffId", staff.getStaffId());
        summary.put("staffName", staff.getFirstName() + " " + (staff.getOtherName() != null ? staff.getOtherName() : ""));
        summary.put("staffEmail", staff.getEmail());
        summary.put("staffCode", staff.getStaffCode());

        if (staff.getBranch() != null) {
            summary.put("branchId", staff.getBranch().getBranchId());
            summary.put("branchName", staff.getBranch().getBranchName());
        } else {
            summary.put("branchName", "Not Assigned");
        }
        if (staff.getDepartment() != null) {
            summary.put("departmentId", staff.getDepartment().getDepartmentId());
            summary.put("departmentName", staff.getDepartment().getDepartmentName());
        } else {
            summary.put("departmentName", "Not Assigned");
        }

        // All task assignments for this specific staff
        List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignUser_StaffId(staff.getStaffId())
                .stream()
                .filter(a -> a.getAssignUser() != null && a.getAssignUser().getStaffId().equals(staff.getStaffId()))
                .collect(Collectors.toList());

        int totalTasks = assignments.size();
        int completedTasks = 0, pendingTasks = 0, inProgressTasks = 0, rejectedTasks = 0, approvedTasks = 0;

        for (TaskAssignment a : assignments) {
            if (a.getStatus() == TaskStatus.APPROVED) {
                approvedTasks++;
                completedTasks++;
            } else if (a.getStatus() == TaskStatus.COMPLETED) {
                completedTasks++;
            } else if (a.getStatus() == TaskStatus.REJECTED) {
                rejectedTasks++;
            } else if (a.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgressTasks++;
            } else {
                pendingTasks++;
            }
        }

        // ✅ FIXED: Get evaluations DIRECTLY by staff_id - COMPLETE ISOLATION
        // This ensures you ONLY get evaluations belonging to THIS specific staff member
        List<Evaluation> evaluations = evaluationRepository.findByStaffId(staff.getStaffId());

        // Average score – skip null scores
        double averageScore = evaluations.stream()
                .filter(e -> e.getScore() != null)
                .mapToDouble(Evaluation::getScore)
                .average()
                .orElse(0.0);

        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;
        String performanceLevel = determinePerformanceLevel(averageScore);

        summary.put("totalTasks", totalTasks);
        summary.put("completedTasks", completedTasks);
        summary.put("pendingTasks", pendingTasks);
        summary.put("inProgressTasks", inProgressTasks);
        summary.put("rejectedTasks", rejectedTasks);
        summary.put("approvedTasks", approvedTasks);
        summary.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        summary.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        summary.put("totalEvaluations", evaluations.size());
        summary.put("performanceLevel", performanceLevel);

        // Recent evaluations (max 5) - now using staff_id filtered list
        List<Map<String, Object>> recentEvals = evaluations.stream()
                .sorted(Comparator.comparing(Evaluation::getEvaluationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("evaluationId", e.getEvaluationId());
                    map.put("evaluationCode", e.getEvaluationCode());
                    map.put("score", e.getScore());
                    map.put("remarks", e.getRemarks());
                    map.put("evaluationDate", e.getEvaluationDate());
                    map.put("quarter", e.getQuarter());
                    map.put("year", e.getYear());
                    map.put("taskCode", e.getTask() != null ? e.getTask().getTaskCode() : null);
                    map.put("taskId", e.getTask() != null ? e.getTask().getTaskId() : null);
                    return map;
                })
                .collect(Collectors.toList());
        summary.put("recentEvaluations", recentEvals);

        log.info("Staff {} (ID: {}): totalTasks={}, completed={}, avgScore={}, evaluations={}",
                staff.getFirstName(), staff.getStaffId(), totalTasks, completedTasks, averageScore, evaluations.size());
        return summary;
    }

    // =========================
    // DETAILED VIEW
    // =========================

    public Map<String, Object> getStaffEvaluationDetails(Long staffId) {
        User staff = userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
        if (staff.getRole() != Role.STAFF) throw new RuntimeException("User is not a staff member");

        Map<String, Object> details = getStaffEvaluationSummary(staff);
        List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignUser_StaffId(staffId)
                .stream()
                .filter(a -> a.getAssignUser().getStaffId().equals(staffId))
                .collect(Collectors.toList());

        List<Map<String, Object>> taskDetails = assignments.stream()
                .map(a -> {
                    Map<String, Object> tm = new HashMap<>();
                    Task t = a.getTask();
                    tm.put("assignmentId", a.getId());
                    tm.put("taskId", t.getTaskId());
                    tm.put("taskTitle", t.getDescription() != null ? t.getDescription() : "Task #" + t.getTaskId());
                    tm.put("taskDescription", t.getDescription());
                    tm.put("taskCode", t.getTaskCode());
                    tm.put("status", a.getStatus().toString());
                    tm.put("assignedDate", a.getAssignedAt());
                    tm.put("dueDate", t.getDeadline());

                    // ✅ FIXED: Use the repository method to get evaluation by staff_id AND task_id
                    Optional<Evaluation> evaluationOpt = evaluationRepository.findByStaffIdAndTask_TaskId(staffId, t.getTaskId());
                    evaluationOpt.ifPresent(e -> {
                        tm.put("evaluationId", e.getEvaluationId());
                        tm.put("evaluationCode", e.getEvaluationCode());
                        tm.put("score", e.getScore());
                        tm.put("remarks", e.getRemarks());
                        tm.put("evaluationDate", e.getEvaluationDate());
                        tm.put("quarter", e.getQuarter());
                        tm.put("year", e.getYear());
                    });
                    return tm;
                })
                .collect(Collectors.toList());
        details.put("taskDetails", taskDetails);
        return details;
    }

    // =========================
    // CREATE EVALUATION – STRICT STAFF ISOLATION
    // =========================

    @Transactional
    public EvaluationDTOResponse createStaffEvaluation(Long staffId, Long taskId, Double score, String remarks, Integer quarter, Integer year) {
        User staff = userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));
        if (staff.getRole() != Role.STAFF) throw new RuntimeException("User is not a staff member");

        // 1. Task must be assigned to this staff
        TaskAssignment assignment = taskAssignmentRepository.findByStaffIdAndTask_TaskId(staffId, taskId)
                .orElseThrow(() -> new RuntimeException("Task " + taskId + " is not assigned to staff " + staffId));

        // 2. Extra safety: check assignee
        if (!assignment.getAssignUser().getStaffId().equals(staffId)) {
            throw new RuntimeException("Task does not belong to this staff member (staffId=" + staffId + ")");
        }

        // 3. No duplicate evaluation for same staff, task, quarter, year
        Integer evalYear = year != null ? year : Year.now().getValue();
        Integer evalQuarter = quarter != null ? quarter : getCurrentQuarter();

        // ✅ Use the repository method for duplicate check
        boolean alreadyExists = evaluationRepository.existsByStaffIdAndTaskIdAndYearAndQuarter(
                staffId, taskId, evalYear, evalQuarter);

        if (alreadyExists) {
            throw new RuntimeException("Evaluation already exists for this staff on task " + taskId +
                    " in Q" + evalQuarter + " " + evalYear);
        }

        Supervisor supervisor = assignment.getTask().getSupervisor();
        if (supervisor == null) throw new RuntimeException("No supervisor assigned to this task");

        EvaluationDTO dto = new EvaluationDTO();
        dto.setTask_id(taskId);
        dto.setStaffId(staffId);  // ✅ CRITICAL: Set staff_id for isolation
        dto.setSupervisorId(supervisor.getSupervisorId());
        if (score != null) dto.setScore(score);
        dto.setRemarks(remarks);
        dto.setEvaluationDate(LocalDateTime.now());
        dto.setYear(evalYear);
        dto.setQuarter(evalQuarter);

        log.info("Creating evaluation for staff {} (ID {}) on task {} in Q{}/{}",
                staff.getFirstName(), staffId, taskId, evalQuarter, evalYear);
        return evaluationService.createEvaluation(dto);
    }

    // =========================
    // UPDATE EVALUATION
    // =========================

    @Transactional
    public Optional<EvaluationDTOResponse> updateStaffEvaluation(Long evaluationId, Double score, String remarks) {
        return evaluationRepository.findById(evaluationId).map(evaluation -> {
            // Verify this evaluation belongs to the correct staff (security check)
            if (evaluation.getStaffId() == null) {
                throw new RuntimeException("Evaluation has no staff association");
            }

            EvaluationDTO dto = new EvaluationDTO();
            dto.setTask_id(evaluation.getTask().getTaskId());
            dto.setStaffId(evaluation.getStaffId());  // ✅ Preserve staff ID
            dto.setSupervisorId(evaluation.getSupervisor().getSupervisorId());
            dto.setScore(score);
            dto.setRemarks(remarks);
            dto.setEvaluationDate(LocalDateTime.now());
            dto.setYear(evaluation.getYear());
            dto.setQuarter(evaluation.getQuarter());

            log.info("Updating evaluation {} for staff {} on task {}",
                    evaluationId, evaluation.getStaffId(), evaluation.getTask().getTaskId());
            return evaluationService.updateEvaluation(evaluationId, dto).orElse(null);
        });
    }

    // =========================
    // DEPARTMENT STATS (aggregate)
    // =========================

    public Map<String, Object> getDepartmentEvaluationStats(Long departmentId) {
        List<User> staffMembers = (departmentId != null)
                ? userRepository.findStaffByDepartmentId(departmentId)
                : userRepository.findByRole(Role.STAFF);

        double totalScore = 0, totalCompletion = 0;
        Map<String, Integer> perfDist = new HashMap<>();
        perfDist.put("Excellent", 0);
        perfDist.put("Good", 0);
        perfDist.put("Satisfactory", 0);
        perfDist.put("Needs Improvement", 0);
        perfDist.put("Poor", 0);
        perfDist.put("Not Evaluated", 0);

        for (User staff : staffMembers) {
            Map<String, Object> sum = getStaffEvaluationSummary(staff);
            totalScore += (double) sum.get("averageScore");
            totalCompletion += (double) sum.get("completionRate");
            String level = (String) sum.get("performanceLevel");
            perfDist.merge(level, 1, Integer::sum);
        }

        int count = staffMembers.size();
        Map<String, Object> result = new HashMap<>();
        result.put("totalStaff", count);
        result.put("averageScore", count > 0 ? Math.round(totalScore / count * 100.0) / 100.0 : 0.0);
        result.put("averageCompletionRate", count > 0 ? Math.round(totalCompletion / count * 100.0) / 100.0 : 0.0);
        result.put("performanceDistribution", perfDist);
        return result;
    }

    // =========================
    // QUARTERLY REPORTS
    // =========================

    public List<Map<String, Object>> getStaffQuarterlyReport(Long staffId, Integer year, Integer quarter) {
        // ✅ FIXED: Get evaluations directly by staff_id
        List<Evaluation> evaluations;
        if (year != null && quarter != null) {
            evaluations = evaluationRepository.findByStaffIdAndYearAndQuarter(staffId, year, quarter);
        } else if (year != null) {
            evaluations = evaluationRepository.findByStaffIdAndYear(staffId, year);
        } else {
            evaluations = evaluationRepository.findByStaffId(staffId);
        }

        return evaluations.stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("evaluationId", e.getEvaluationId());
                    map.put("evaluationCode", e.getEvaluationCode());
                    map.put("taskId", e.getTask() != null ? e.getTask().getTaskId() : null);
                    map.put("taskDescription", e.getTask() != null ? e.getTask().getDescription() : null);
                    map.put("taskCode", e.getTask() != null ? e.getTask().getTaskCode() : null);
                    map.put("score", e.getScore());
                    map.put("remarks", e.getRemarks());
                    map.put("evaluationDate", e.getEvaluationDate());
                    map.put("quarter", e.getQuarter());
                    map.put("year", e.getYear());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getQuarterlyReport(Integer year, Integer quarter) {
        // Get all evaluations for the specified period
        List<Evaluation> evaluations = (year != null && quarter != null)
                ? evaluationRepository.findByYearAndQuarter(year, quarter)
                : (year != null) ? evaluationRepository.findByYear(year) : evaluationRepository.findAll();

        Map<Long, Map<String, Object>> staffStats = new HashMap<>();

        for (Evaluation e : evaluations) {
            Long staffId = e.getStaffId();
            if (staffId == null) continue;

            // Get staff info
            User staff = userRepository.findByStaffId(staffId).orElse(null);
            if (staff == null || staff.getRole() != Role.STAFF) continue;

            final Long finalStaffId = staffId;
            staffStats.computeIfAbsent(staffId, id -> {
                Map<String, Object> s = new HashMap<>();
                s.put("staffId", id);
                s.put("staffName", staff.getFirstName() + " " + (staff.getOtherName() != null ? staff.getOtherName() : ""));
                s.put("staffCode", staff.getStaffCode());
                s.put("branchName", staff.getBranch() != null ? staff.getBranch().getBranchName() : "Not Assigned");
                s.put("departmentName", staff.getDepartment() != null ? staff.getDepartment().getDepartmentName() : "Not Assigned");
                s.put("totalScore", 0.0);
                s.put("count", 0);
                return s;
            });

            Map<String, Object> stats = staffStats.get(staffId);
            double total = (double) stats.get("totalScore") + (e.getScore() != null ? e.getScore() : 0);
            int cnt = (int) stats.get("count") + 1;
            stats.put("totalScore", total);
            stats.put("count", cnt);
            double avgScore = total / cnt;
            stats.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
            stats.put("performanceLevel", determinePerformanceLevel(avgScore));
        }
        return new ArrayList<>(staffStats.values());
    }

    // =========================
    // ADDITIONAL HELPER METHODS FOR STAFF ISOLATION
    // =========================

    /**
     * Get all evaluations for a specific staff member as DTO responses
     * Uses the existing EvaluationService method
     */
    public List<EvaluationDTOResponse> getEvaluationsByStaffId(Long staffId) {
        return evaluationService.getEvaluationsByStaffId(staffId);
    }

    /**
     * Check if a staff member has an evaluation for a specific task and quarter
     */
    public boolean hasEvaluationForTaskAndQuarter(Long staffId, Long taskId, Integer year, Integer quarter) {
        return evaluationRepository.existsByStaffIdAndTaskIdAndYearAndQuarter(staffId, taskId, year, quarter);
    }

    /**
     * Get average score for a staff member across all evaluations
     */
    public Double getStaffAverageScore(Long staffId) {
        return evaluationService.getAverageScoreByStaffId(staffId);
    }

    // =========================
    // HELPERS
    // =========================

    private String determinePerformanceLevel(double averageScore) {
        if (averageScore == 0.0) return "Not Evaluated";
        if (averageScore >= 90) return "Excellent";
        if (averageScore >= 75) return "Good";
        if (averageScore >= 60) return "Satisfactory";
        if (averageScore >= 40) return "Needs Improvement";
        return "Poor";
    }

    private Integer getCurrentQuarter() {
        int month = LocalDateTime.now().getMonthValue();
        return (month <= 3) ? 1 : (month <= 6) ? 2 : (month <= 9) ? 3 : 4;
    }
}