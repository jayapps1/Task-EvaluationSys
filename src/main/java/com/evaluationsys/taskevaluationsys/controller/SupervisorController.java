package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.SupervisorDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.SupervisorDTOResponse;
import com.evaluationsys.taskevaluationsys.service.SupervisorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/supervisors")
public class SupervisorController {

    private final SupervisorService supervisorService;

    public SupervisorController(SupervisorService supervisorService) {
        this.supervisorService = supervisorService;
    }

    // ------------------------------
    // GET ALL SUPERVISORS
    // ------------------------------
    @GetMapping
    public ResponseEntity<List<SupervisorDTOResponse>> getAllSupervisors() {
        return ResponseEntity.ok(supervisorService.getAllSupervisors());
    }

    // ------------------------------
    // GET BY CODE (SAFE)
    // ------------------------------
    @GetMapping("/{supervisorCode:.+}")
    public ResponseEntity<SupervisorDTOResponse> getSupervisorByCode(
            @PathVariable String supervisorCode) {

        return supervisorService.getSupervisorByCode(supervisorCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------
    // CREATE
    // ------------------------------
    @PostMapping
    public ResponseEntity<SupervisorDTOResponse> createSupervisor(
            @Valid @RequestBody SupervisorDTO dto) {

        SupervisorDTOResponse response =
                supervisorService.createSupervisorByCodes(dto);

        return ResponseEntity.created(
                        URI.create("/supervisors/" + response.getSupervisorCode()))
                .body(response);
    }

    // ------------------------------
    // UPDATE (FULL FIX FOR SLASHES)
    // ------------------------------
    @PutMapping("/**")
    public ResponseEntity<SupervisorDTOResponse> updateSupervisor(
            HttpServletRequest request,
            @Valid @RequestBody SupervisorDTO dto) {

        String supervisorCode = extractSupervisorCode(request);

        return supervisorService.updateSupervisorByCodes(supervisorCode, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------
    // DELETE (FULL FIX FOR SLASHES)
    // ------------------------------
    @DeleteMapping("/**")
    public ResponseEntity<String> deleteSupervisor(HttpServletRequest request) {

        String supervisorCode = extractSupervisorCode(request);

        Optional<SupervisorDTOResponse> supervisor =
                supervisorService.getSupervisorByCode(supervisorCode);

        if (supervisor.isPresent()) {
            supervisorService.deleteSupervisorByCode(supervisorCode);
            return ResponseEntity.ok("Supervisor deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------------------
    // HELPER METHOD (CRITICAL)
    // ------------------------------
    private String extractSupervisorCode(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Extract everything after "/supervisors/"
        return path.substring(path.indexOf("/supervisors/") + 13);
    }
}