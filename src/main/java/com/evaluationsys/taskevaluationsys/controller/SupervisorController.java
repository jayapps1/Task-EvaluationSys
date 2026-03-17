package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.SupervisorDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.SupervisorDTOResponse;
import com.evaluationsys.taskevaluationsys.service.SupervisorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/supervisors")
public class SupervisorController {

    private final SupervisorService supervisorService;

    public SupervisorController(SupervisorService supervisorService) {
        this.supervisorService = supervisorService;
    }

    // GET ALL SUPERVISORS
    @GetMapping
    public ResponseEntity<List<SupervisorDTOResponse>> getAllSupervisors() {
        return ResponseEntity.ok(supervisorService.getAllSupervisors());
    }

    // GET SUPERVISOR BY CODE
    @GetMapping("/by-code")
    public ResponseEntity<SupervisorDTOResponse> getSupervisorByCode(@RequestParam String supervisorCode) {
        return supervisorService.getSupervisorByCode(supervisorCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE SUPERVISOR
    @PostMapping
    public ResponseEntity<SupervisorDTOResponse> createSupervisor(@RequestBody SupervisorDTO dto) {
        SupervisorDTOResponse response = supervisorService.createSupervisor(dto);

        return ResponseEntity
                .created(URI.create("/supervisors/by-code?supervisorCode=" + response.getSupervisorCode()))
                .body(response);
    }

    // UPDATE SUPERVISOR
    @PutMapping("/update")
    public ResponseEntity<SupervisorDTOResponse> updateSupervisor(
            @RequestParam String supervisorCode,
            @RequestBody SupervisorDTO dto) {

        return supervisorService.updateSupervisor(supervisorCode, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE SUPERVISOR
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteSupervisor(@RequestParam String supervisorCode) {
        supervisorService.deleteSupervisor(supervisorCode);
        return ResponseEntity.noContent().build();
    }
}