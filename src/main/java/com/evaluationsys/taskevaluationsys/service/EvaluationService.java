package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.EvaluationDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.EvaluationDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Evaluation;
import com.evaluationsys.taskevaluationsys.entity.Supervisor;
import com.evaluationsys.taskevaluationsys.entity.Task;
import com.evaluationsys.taskevaluationsys.repository.EvaluationRepository;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.TaskRepository;
import org.springframework.stereotype.Service;

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

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            TaskRepository taskRepository,
            SupervisorRepository supervisorRepository) {
        this.evaluationRepository = evaluationRepository;
        this.taskRepository = taskRepository;
        this.supervisorRepository = supervisorRepository;
    }

    // GENERATE CODE
    private String generateEvaluationCode(Integer year) {

        long count = evaluationRepository.countByYear(year) + 1;

        String yearShort = String.valueOf(year).substring(2);

        return String.format("EVA/%s/%03d", yearShort, count);
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

    // CREATE
    public EvaluationDTOResponse createEvaluation(EvaluationDTO dto) {

        Task task = taskRepository.findById(dto.getTask_id())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Supervisor supervisor = supervisorRepository.findById(dto.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Integer year = dto.getYear() != null ? dto.getYear() : Year.now().getValue();

        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationCode(generateEvaluationCode(year));
        evaluation.setTask(task);
        evaluation.setSupervisor(supervisor);
        evaluation.setScore(dto.getScore());
        evaluation.setRemarks(dto.getRemarks());
        evaluation.setEvaluationDate(dto.getEvaluationDate());
        evaluation.setYear(year);
        evaluation.setQuarter(dto.getQuarter());
        evaluation.setCreatedAt(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(evaluation);

        return mapToResponse(saved);
    }

    // UPDATE
    public Optional<EvaluationDTOResponse> updateEvaluation(Long id, EvaluationDTO dto) {

        return evaluationRepository.findById(id).map(evaluation -> {

            Task task = taskRepository.findById(dto.getTask_id())
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            Supervisor supervisor = supervisorRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));

            evaluation.setTask(task);
            evaluation.setSupervisor(supervisor);
            evaluation.setScore(dto.getScore());
            evaluation.setRemarks(dto.getRemarks());
            evaluation.setEvaluationDate(dto.getEvaluationDate());
            evaluation.setYear(dto.getYear());
            evaluation.setQuarter(dto.getQuarter());
            evaluation.setUpdatedAt(LocalDateTime.now());

            Evaluation saved = evaluationRepository.save(evaluation);

            return mapToResponse(saved);
        });
    }

    // DELETE
    public void deleteEvaluation(Long id) {
        evaluationRepository.deleteById(id);
    }

    // MAP ENTITY → DTO
    private EvaluationDTOResponse mapToResponse(Evaluation evaluation) {

        EvaluationDTOResponse response = new EvaluationDTOResponse();

        response.setEvaluationId(evaluation.getEvaluationId());
        response.setEvaluationCode(evaluation.getEvaluationCode());
        response.setTask_id(evaluation.getTask().getTask_id());
        response.setSupervisorId(evaluation.getSupervisor().getSupervisorId());
        response.setScore(evaluation.getScore());
        response.setRemarks(evaluation.getRemarks());
        response.setEvaluationDate(evaluation.getEvaluationDate());
        response.setYear(evaluation.getYear());
        response.setQuarter(evaluation.getQuarter());

        return response;
    }
}