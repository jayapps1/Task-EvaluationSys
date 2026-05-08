package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.EvaluationDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.EvaluationDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Evaluation;
import com.evaluationsys.taskevaluationsys.entity.Supervisor;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.repository.EvaluationRepository;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskAssignmentRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final TaskRepository taskRepository;
    private final SupervisorRepository supervisorRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            TaskRepository taskRepository,
            SupervisorRepository supervisorRepository,
            TaskAssignmentRepository taskAssignmentRepository) {
        this.evaluationRepository = evaluationRepository;
        this.taskRepository = taskRepository;
        this.supervisorRepository = supervisorRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    // GENERATE CODE
    private String generateEvaluationCode(Integer year) {
        long count = evaluationRepository.countByYear(year) + 1;
        String yearShort = String.valueOf(year).substring(2);
        return String.format("EVA/%s/%03d", yearShort, count);
    }

    private Integer getCurrentQuarter() {
        int month = LocalDateTime.now().getMonthValue();
        if (month <= 3) return 1;
        else if (month <= 6) return 2;
        else if (month <= 9) return 3;
        else return 4;
    }

    // GET ALL
    public List<EvaluationDTOResponse> getAllEvaluations() {
        return evaluationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public Optional<EvaluationDTOResponse> getEvaluationById(Long id) {
        return evaluationRepository.findById(id)
                .map(this::mapToResponse);
    }

    // ✅ CREATE - WITH STAFF ISOLATION
    @Transactional
    public EvaluationDTOResponse createEvaluation(EvaluationDTO dto) {

        // Validate task exists
        Task task = taskRepository.findById(dto.getTask_id())
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + dto.getTask_id()));

        // Validate supervisor exists
        Supervisor supervisor = supervisorRepository.findById(dto.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("Supervisor not found with ID: " + dto.getSupervisorId()));

        // ✅ CRITICAL: Validate that this task is actually assigned to this staff
        if (dto.getStaffId() != null) {
            boolean isAssigned = taskAssignmentRepository.existsByTask_TaskIdAndAssignUser_StaffId(
                    dto.getTask_id(), dto.getStaffId());

            if (!isAssigned) {
                throw new RuntimeException("Task " + dto.getTask_id() + " is not assigned to staff " + dto.getStaffId());
            }
        }

        Integer year = dto.getYear() != null ? dto.getYear() : Year.now().getValue();
        Integer quarter = dto.getQuarter() != null ? dto.getQuarter() : getCurrentQuarter();

        // ✅ Check for duplicate evaluation for this staff, task, and quarter
        if (dto.getStaffId() != null) {
            boolean alreadyExists = evaluationRepository.existsByStaffIdAndTaskIdAndYearAndQuarter(
                    dto.getStaffId(), dto.getTask_id(), year, quarter);

            if (alreadyExists) {
                throw new RuntimeException("Evaluation already exists for this task in Q" + quarter + " " + year);
            }
        } else {
            // Fallback check for existing evaluations without staff_id
            boolean alreadyExists = evaluationRepository.existsByTask_TaskIdAndYearAndQuarter(
                    dto.getTask_id(), year, quarter);

            if (alreadyExists) {
                throw new RuntimeException("Evaluation already exists for this task in Q" + quarter + " " + year);
            }
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationCode(generateEvaluationCode(year));
        evaluation.setTask(task);
        evaluation.setSupervisor(supervisor);

        // ✅ Store staff ID for isolation
        if (dto.getStaffId() != null) {
            evaluation.setStaffId(dto.getStaffId());
        }

        evaluation.setScore(dto.getScore());
        evaluation.setRemarks(dto.getRemarks());
        evaluation.setEvaluationDate(dto.getEvaluationDate() != null ? dto.getEvaluationDate() : LocalDateTime.now());
        evaluation.setYear(year);
        evaluation.setQuarter(quarter);
        evaluation.setCreatedAt(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(evaluation);

        System.out.println("✅ Evaluation created for staff " + dto.getStaffId() + " on task " + dto.getTask_id() + " in Q" + quarter + " " + year);

        return mapToResponse(saved);
    }

    // ✅ UPDATE - WITH STAFF ISOLATION
    @Transactional
    public Optional<EvaluationDTOResponse> updateEvaluation(Long id, EvaluationDTO dto) {

        return evaluationRepository.findById(id).map(evaluation -> {

            Task task = taskRepository.findById(dto.getTask_id())
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            Supervisor supervisor = supervisorRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));

            // ✅ Verify the evaluation belongs to the correct staff (if staffId provided)
            if (dto.getStaffId() != null && evaluation.getStaffId() != null) {
                if (!evaluation.getStaffId().equals(dto.getStaffId())) {
                    throw new RuntimeException("Evaluation does not belong to this staff member");
                }
            }

            evaluation.setTask(task);
            evaluation.setSupervisor(supervisor);
            evaluation.setScore(dto.getScore());
            evaluation.setRemarks(dto.getRemarks());
            evaluation.setEvaluationDate(dto.getEvaluationDate());
            evaluation.setYear(dto.getYear());
            evaluation.setQuarter(dto.getQuarter());
            evaluation.setUpdatedAt(LocalDateTime.now());

            Evaluation saved = evaluationRepository.save(evaluation);

            System.out.println("✅ Evaluation updated for ID: " + id);

            return mapToResponse(saved);
        });
    }

    // DELETE
    @Transactional
    public void deleteEvaluation(Long id) {
        evaluationRepository.deleteById(id);
        System.out.println("✅ Evaluation deleted for ID: " + id);
    }

    // =========================
    // STAFF-SPECIFIC METHODS (FOR ISOLATION)
    // =========================

    // Get all evaluations for a specific staff member
    public List<EvaluationDTOResponse> getEvaluationsByStaffId(Long staffId) {
        return evaluationRepository.findByStaffId(staffId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get evaluations for a staff member by year and quarter
    public List<EvaluationDTOResponse> getEvaluationsByStaffIdAndQuarter(Long staffId, Integer year, Integer quarter) {
        return evaluationRepository.findByStaffIdAndYearAndQuarter(staffId, year, quarter)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get average score for a specific staff member
    public Double getAverageScoreByStaffId(Long staffId) {
        return evaluationRepository.getAverageScoreByStaffId(staffId);
    }

    // Get recent evaluations for a staff member
    public List<EvaluationDTOResponse> getRecentEvaluationsByStaffId(Long staffId, int limit) {
        return evaluationRepository.findRecentByStaffId(staffId, limit)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // MAP ENTITY → DTO
    private EvaluationDTOResponse mapToResponse(Evaluation evaluation) {

        EvaluationDTOResponse response = new EvaluationDTOResponse();

        response.setEvaluationId(evaluation.getEvaluationId());
        response.setEvaluationCode(evaluation.getEvaluationCode());
        response.setTaskId(evaluation.getTask().getTaskId());
        response.setSupervisorId(evaluation.getSupervisor().getSupervisorId());
        response.setStaffId(evaluation.getStaffId());  // ✅ Include staff ID in response
        response.setScore(evaluation.getScore());
        response.setRemarks(evaluation.getRemarks());
        response.setEvaluationDate(evaluation.getEvaluationDate());
        response.setYear(evaluation.getYear());
        response.setQuarter(evaluation.getQuarter());

        return response;
    }
}