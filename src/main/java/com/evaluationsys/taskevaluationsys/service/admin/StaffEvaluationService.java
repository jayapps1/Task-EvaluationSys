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

    /**
     * Get all staff members with their evaluation summary
     */
    public List<Map<String, Object>> getAllStaffEvaluationSummary() {
        List<User> staffMembers = userRepository.findByRole(Role.STAFF);
        return staffMembers.stream()
                .map(this::getStaffEvaluationSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered staff evaluation summary
     */
    public List<Map<String, Object>> getFilteredStaffEvaluationSummary(Long branchId, Long departmentId) {
        List<User> staffMembers;

        if (branchId != null && departmentId != null) {
            staffMembers = userRepository.findByRoleAndBranchIdAndDepartmentId(
                    Role.STAFF, branchId, departmentId);
        } else if (branchId != null) {
            staffMembers = userRepository.findByRoleAndBranchId(Role.STAFF, branchId);
        } else if (departmentId != null) {
            staffMembers = userRepository.findByRoleAndDepartmentId(Role.STAFF, departmentId);
        } else {
            staffMembers = userRepository.findByRole(Role.STAFF);
        }

        return staffMembers.stream()
                .map(this::getStaffEvaluationSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get evaluation summary for a single staff member - COMPLETELY ISOLATED
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

        // Get ALL task assignments for this specific staff ONLY
        List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignUser_StaffId(staff.getStaffId());

        // Double verify each assignment belongs to this staff
        assignments = assignments.stream()
                .filter(a -> a.getAssignUser().getStaffId().equals(staff.getStaffId()))
                .collect(Collectors.toList());

        int totalTasks = assignments.size();
        int completedTasks = 0;
        int pendingTasks = 0;
        int inProgressTasks = 0;
        int rejectedTasks = 0;
        int approvedTasks = 0;

        for (TaskAssignment assignment : assignments) {
            if (assignment.getStatus() == TaskStatus.APPROVED) {
                approvedTasks++;
                completedTasks++;
            } else if (assignment.getStatus() == TaskStatus.COMPLETED) {
                completedTasks++;
            } else if (assignment.getStatus() == TaskStatus.REJECTED) {
                rejectedTasks++;
            } else if (assignment.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgressTasks++;
            } else {
                pendingTasks++;
            }
        }

        // Get evaluations ONLY for this staff's tasks
        List<Evaluation> evaluations = new ArrayList<>();
        for (TaskAssignment assignment : assignments) {
            // Get evaluations for this specific task
            List<Evaluation> evalList = evaluationRepository.findByTask_TaskId(assignment.getTask().getTaskId());
            // Filter evaluations that belong to this staff's task (should already be correct, but double filter)
            for (Evaluation eval : evalList) {
                // Verify this evaluation is actually for this staff member through task assignment
                Optional<TaskAssignment> verifyAssignment = taskAssignmentRepository
                        .findByStaffIdAndTask_TaskId(staff.getStaffId(), eval.getTask().getTaskId());
                if (verifyAssignment.isPresent()) {
                    evaluations.add(eval);
                }
            }
        }

        double averageScore = evaluations.stream()
                .mapToDouble(Evaluation::getScore)
                .average()
                .orElse(0.0);

        int totalEvaluations = evaluations.size();

        // Calculate completion rate
        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;

        // Determine performance level
        String performanceLevel = determinePerformanceLevel(averageScore);

        summary.put("totalTasks", totalTasks);
        summary.put("completedTasks", completedTasks);
        summary.put("pendingTasks", pendingTasks);
        summary.put("inProgressTasks", inProgressTasks);
        summary.put("rejectedTasks", rejectedTasks);
        summary.put("approvedTasks", approvedTasks);
        summary.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        summary.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        summary.put("totalEvaluations", totalEvaluations);
        summary.put("performanceLevel", performanceLevel);

        // Get recent evaluations
        List<Map<String, Object>> recentEvals = evaluations.stream()
                .sorted((e1, e2) -> e2.getEvaluationDate().compareTo(e1.getEvaluationDate()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> evalMap = new HashMap<>();
                    evalMap.put("evaluationId", e.getEvaluationId());
                    evalMap.put("evaluationCode", e.getEvaluationCode());
                    evalMap.put("score", e.getScore());
                    evalMap.put("remarks", e.getRemarks());
                    evalMap.put("evaluationDate", e.getEvaluationDate());
                    evalMap.put("quarter", e.getQuarter());
                    evalMap.put("year", e.getYear());
                    // Get task info for display
                    Optional<TaskAssignment> taskAssign = taskAssignmentRepository
                            .findByStaffIdAndTask_TaskId(staff.getStaffId(), e.getTask().getTaskId());
                    evalMap.put("taskCode", taskAssign.map(ta -> ta.getTask().getTaskCode()).orElse("N/A"));
                    return evalMap;
                })
                .collect(Collectors.toList());

        summary.put("recentEvaluations", recentEvals);

        log.info("Staff {}: totalTasks={}, completedTasks={}, averageScore={}, evaluations={}",
                staff.getFirstName(), totalTasks, completedTasks, averageScore, evaluations.size());

        return summary;
    }

    /**
     * Get detailed evaluation for a specific staff member
     */
    public Map<String, Object> getStaffEvaluationDetails(Long staffId) {
        User staff = userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));

        if (staff.getRole() != Role.STAFF) {
            throw new RuntimeException("User is not a staff member");
        }

        Map<String, Object> details = getStaffEvaluationSummary(staff);

        // Get all task assignments with their evaluations - ONLY for this staff
        List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignUser_StaffId(staffId);

        List<Map<String, Object>> taskDetails = assignments.stream()
                .filter(assignment -> assignment.getAssignUser().getStaffId().equals(staffId))
                .map(assignment -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    Task task = assignment.getTask();

                    taskMap.put("assignmentId", assignment.getId());
                    taskMap.put("taskId", task.getTaskId());
                    taskMap.put("taskTitle", task.getDescription() != null ? task.getDescription() : "Task #" + task.getTaskId());
                    taskMap.put("taskDescription", task.getDescription() != null ? task.getDescription() : "");
                    taskMap.put("taskCode", task.getTaskCode());
                    taskMap.put("status", assignment.getStatus().toString());
                    taskMap.put("assignedDate", assignment.getAssignedAt());
                    taskMap.put("dueDate", task.getDeadline());

                    // Get evaluation for this task - verify it's for this staff
                    List<Evaluation> evaluations = evaluationRepository.findByTask_TaskId(task.getTaskId());
                    Optional<Evaluation> staffEval = evaluations.stream()
                            .filter(e -> {
                                Optional<TaskAssignment> ta = taskAssignmentRepository
                                        .findByStaffIdAndTask_TaskId(staffId, e.getTask().getTaskId());
                                return ta.isPresent();
                            })
                            .findFirst();

                    if (staffEval.isPresent()) {
                        Evaluation eval = staffEval.get();
                        taskMap.put("evaluationId", eval.getEvaluationId());
                        taskMap.put("evaluationCode", eval.getEvaluationCode());
                        taskMap.put("score", eval.getScore());
                        taskMap.put("remarks", eval.getRemarks());
                        taskMap.put("evaluationDate", eval.getEvaluationDate());
                    } else {
                        taskMap.put("evaluationId", null);
                        taskMap.put("evaluationCode", null);
                        taskMap.put("score", null);
                        taskMap.put("remarks", null);
                        taskMap.put("evaluationDate", null);
                    }

                    return taskMap;
                })
                .collect(Collectors.toList());

        details.put("taskDetails", taskDetails);

        return details;
    }

    /**
     * Create evaluation for a staff member - With strict staff isolation
     */
    @Transactional
    public EvaluationDTOResponse createStaffEvaluation(Long staffId, Long taskId, Double score, String remarks, Integer quarter, Integer year) {

        // Verify staff exists
        User staff = userRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));

        if (staff.getRole() != Role.STAFF) {
            throw new RuntimeException("User is not a staff member");
        }

        // Verify task assignment exists for this specific staff
        Optional<TaskAssignment> assignmentOpt = taskAssignmentRepository.findByStaffIdAndTask_TaskId(staffId, taskId);
        TaskAssignment assignment = assignmentOpt
                .orElseThrow(() -> new RuntimeException("Task not assigned to this staff"));

        // Strict verification: ensure this task belongs ONLY to this staff member
        if (!assignment.getAssignUser().getStaffId().equals(staffId)) {
            throw new RuntimeException("Task does not belong to this staff member");
        }

        // Check if evaluation already exists for this task in the same quarter
        Integer evalYear = year != null ? year : Year.now().getValue();
        Integer evalQuarter = quarter != null ? quarter : getCurrentQuarter();

        List<Evaluation> existingEvals = evaluationRepository.findByTask_TaskId(taskId);
        for (Evaluation existing : existingEvals) {
            // Verify this existing evaluation is for this staff member
            Optional<TaskAssignment> existingAssignment = taskAssignmentRepository
                    .findByStaffIdAndTask_TaskId(staffId, existing.getTask().getTaskId());
            if (existingAssignment.isPresent() &&
                    existing.getYear().equals(evalYear) &&
                    existing.getQuarter().equals(evalQuarter)) {
                throw new RuntimeException("Evaluation already exists for this task in Q" + evalQuarter + " " + evalYear);
            }
        }

        // Get supervisor from task
        Supervisor supervisor = assignment.getTask().getSupervisor();
        if (supervisor == null) {
            throw new RuntimeException("No supervisor assigned to this task");
        }

        // Create evaluation DTO
        EvaluationDTO evaluationDTO = new EvaluationDTO();
        evaluationDTO.setTask_id(taskId);
        evaluationDTO.setSupervisorId(supervisor.getSupervisorId());
        if (score != null) {
            evaluationDTO.setScore(score);
        }
        evaluationDTO.setRemarks(remarks);
        evaluationDTO.setEvaluationDate(LocalDateTime.now());
        evaluationDTO.setYear(evalYear);
        evaluationDTO.setQuarter(evalQuarter);

        log.info("Creating evaluation for staff {} (ID: {}) for task {} in Q{}/{} - This evaluation belongs ONLY to this staff",
                staff.getFirstName(), staffId, taskId, evalQuarter, evalYear);

        // Save evaluation
        return evaluationService.createEvaluation(evaluationDTO);
    }

    /**
     * Update existing evaluation - with staff verification (FIXED)
     */
    @Transactional
    public Optional<EvaluationDTOResponse> updateStaffEvaluation(Long evaluationId, Double score, String remarks) {
        return evaluationRepository.findById(evaluationId).map(evaluation -> {
            // Verify this evaluation belongs to the correct staff via task assignment
            // findByTask_TaskId returns List, not Optional
            List<TaskAssignment> assignments = taskAssignmentRepository.findByTask_TaskId(evaluation.getTask().getTaskId());
            if (assignments.isEmpty()) {
                throw new RuntimeException("Associated task assignment not found");
            }

            EvaluationDTO updateDTO = new EvaluationDTO();
            updateDTO.setTask_id(evaluation.getTask().getTaskId());
            updateDTO.setSupervisorId(evaluation.getSupervisor().getSupervisorId());
            updateDTO.setScore(score);
            updateDTO.setRemarks(remarks);
            updateDTO.setEvaluationDate(LocalDateTime.now());
            updateDTO.setYear(evaluation.getYear());
            updateDTO.setQuarter(evaluation.getQuarter());

            log.info("Updating evaluation {} for task {}", evaluationId, evaluation.getTask().getTaskId());

            return evaluationService.updateEvaluation(evaluationId, updateDTO).orElse(null);
        });
    }

    /**
     * Get department-wise evaluation statistics - AGGREGATE ONLY, not affecting individual staff
     */
    public Map<String, Object> getDepartmentEvaluationStats(Long departmentId) {
        List<User> staffMembers;

        if (departmentId != null) {
            staffMembers = userRepository.findStaffByDepartmentId(departmentId);
        } else {
            staffMembers = userRepository.findByRole(Role.STAFF);
        }

        Map<String, Object> stats = new HashMap<>();

        double avgScore = 0.0;
        double avgCompletionRate = 0.0;
        int totalStaff = staffMembers.size();

        Map<String, Integer> performanceDistribution = new HashMap<>();
        performanceDistribution.put("Excellent", 0);
        performanceDistribution.put("Good", 0);
        performanceDistribution.put("Satisfactory", 0);
        performanceDistribution.put("Needs Improvement", 0);
        performanceDistribution.put("Poor", 0);
        performanceDistribution.put("Not Evaluated", 0);

        for (User staff : staffMembers) {
            Map<String, Object> summary = getStaffEvaluationSummary(staff);
            avgScore += (double) summary.get("averageScore");
            avgCompletionRate += (double) summary.get("completionRate");

            String level = (String) summary.get("performanceLevel");
            performanceDistribution.put(level, performanceDistribution.getOrDefault(level, 0) + 1);
        }

        stats.put("totalStaff", totalStaff);
        stats.put("averageScore", totalStaff > 0 ? Math.round(avgScore / totalStaff * 100.0) / 100.0 : 0.0);
        stats.put("averageCompletionRate", totalStaff > 0 ? Math.round(avgCompletionRate / totalStaff * 100.0) / 100.0 : 0.0);
        stats.put("performanceDistribution", performanceDistribution);

        return stats;
    }

    /**
     * Get quarterly evaluation report for a specific staff
     */
    public List<Map<String, Object>> getStaffQuarterlyReport(Long staffId, Integer year, Integer quarter) {
        List<Evaluation> evaluations;

        if (year != null && quarter != null) {
            evaluations = evaluationRepository.findByYearAndQuarter(year, quarter);
        } else if (year != null) {
            evaluations = evaluationRepository.findByYear(year);
        } else {
            evaluations = evaluationRepository.findAll();
        }

        List<Map<String, Object>> staffEvals = new ArrayList<>();

        for (Evaluation evaluation : evaluations) {
            // Verify this evaluation belongs to this staff member
            Optional<TaskAssignment> assignment = taskAssignmentRepository
                    .findByStaffIdAndTask_TaskId(staffId, evaluation.getTask().getTaskId());
            if (assignment.isPresent()) {
                Map<String, Object> evalMap = new HashMap<>();
                evalMap.put("evaluationId", evaluation.getEvaluationId());
                evalMap.put("evaluationCode", evaluation.getEvaluationCode());
                evalMap.put("taskId", evaluation.getTask().getTaskId());
                evalMap.put("taskDescription", evaluation.getTask().getDescription());
                evalMap.put("score", evaluation.getScore());
                evalMap.put("remarks", evaluation.getRemarks());
                evalMap.put("evaluationDate", evaluation.getEvaluationDate());
                evalMap.put("quarter", evaluation.getQuarter());
                evalMap.put("year", evaluation.getYear());
                staffEvals.add(evalMap);
            }
        }

        return staffEvals;
    }

    /**
     * Get quarterly evaluation report for all staff
     */
    public List<Map<String, Object>> getQuarterlyReport(Integer year, Integer quarter) {
        List<Evaluation> evaluations;

        if (year != null && quarter != null) {
            evaluations = evaluationRepository.findByYearAndQuarter(year, quarter);
        } else if (year != null) {
            evaluations = evaluationRepository.findByYear(year);
        } else {
            evaluations = evaluationRepository.findAll();
        }

        Map<Long, Map<String, Object>> staffStats = new HashMap<>();

        for (Evaluation evaluation : evaluations) {
            // Get staff from task assignment
            List<TaskAssignment> assignments = taskAssignmentRepository.findByTask_TaskIdList(evaluation.getTask().getTaskId());
            User staff = assignments.isEmpty() ? null : assignments.get(0).getAssignUser();

            if (staff == null || staff.getRole() != Role.STAFF) continue;

            Map<String, Object> stats = staffStats.getOrDefault(staff.getStaffId(), new HashMap<>());
            stats.put("staffId", staff.getStaffId());
            stats.put("staffName", staff.getFirstName() + " " + (staff.getOtherName() != null ? staff.getOtherName() : ""));
            stats.put("staffCode", staff.getStaffCode());

            double totalScore = (double) stats.getOrDefault("totalScore", 0.0);
            int count = (int) stats.getOrDefault("count", 0);

            totalScore += evaluation.getScore();
            count++;

            stats.put("totalScore", totalScore);
            stats.put("count", count);
            stats.put("averageScore", Math.round((totalScore / count) * 100.0) / 100.0);
            stats.put("performanceLevel", determinePerformanceLevel(totalScore / count));

            staffStats.put(staff.getStaffId(), stats);
        }

        return new ArrayList<>(staffStats.values());
    }

    /**
     * Determine performance level based ONLY on average score
     */
    private String determinePerformanceLevel(double averageScore) {
        if (averageScore == 0.0) {
            return "Not Evaluated";
        } else if (averageScore >= 90) {
            return "Excellent";
        } else if (averageScore >= 75) {
            return "Good";
        } else if (averageScore >= 60) {
            return "Satisfactory";
        } else if (averageScore >= 40) {
            return "Needs Improvement";
        } else {
            return "Poor";
        }
    }

    private Integer getCurrentQuarter() {
        int month = LocalDateTime.now().getMonthValue();
        if (month <= 3) return 1;
        else if (month <= 6) return 2;
        else if (month <= 9) return 3;
        else return 4;
    }
}