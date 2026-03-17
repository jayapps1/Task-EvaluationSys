package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.EvaluationDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.EvaluationDTOResponse;
import com.evaluationsys.taskevaluationsys.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public ResponseEntity<List<EvaluationDTOResponse>> getAllEvaluations() {
        return ResponseEntity.ok(evaluationService.getAllEvaluations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationDTOResponse> getEvaluationById(@PathVariable Long id) {
        return evaluationService.getEvaluationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EvaluationDTOResponse> createEvaluation(@RequestBody EvaluationDTO dto) {

        EvaluationDTOResponse saved = evaluationService.createEvaluation(dto);

        return ResponseEntity.created(URI.create("/evaluations/" + saved.getEvaluationId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvaluationDTOResponse> updateEvaluation(
            @PathVariable Long id,
            @RequestBody EvaluationDTO dto) {

        return evaluationService.updateEvaluation(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {

        evaluationService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }
}