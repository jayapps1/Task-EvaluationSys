package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.entity.Branch;
import com.evaluationsys.taskevaluationsys.service.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    // =========================
    // GET ALL BRANCHES
    // =========================
    @GetMapping
    public ResponseEntity<List<Branch>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    // =========================
    // CREATE NEW BRANCH
    // =========================
    @PostMapping("/create")
    public ResponseEntity<Branch> createBranch(@RequestBody Branch branch) {
        if (branch.getBranchName() == null || branch.getBranchName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Branch savedBranch = branchService.createBranch(branch);

        return ResponseEntity
                .created(URI.create("/branches/by-code?branchCode=" + savedBranch.getBranchCode()))
                .body(savedBranch);
    }

    // =========================
    // GET BRANCH BY ID
    // =========================
    @GetMapping("/by-id")
    public ResponseEntity<Branch> getBranchById(@RequestParam Long branchId) {
        return branchService.getBranchById(branchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // GET BRANCH BY CODE
    // =========================
    @GetMapping("/by-code")
    public ResponseEntity<Branch> getBranchByCode(@RequestParam String branchCode) {
        return branchService.getBranchByCode(branchCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // UPDATE BRANCH
    // =========================
    @PutMapping("/update")
    public ResponseEntity<Branch> updateBranch(@RequestParam String branchCode, @RequestBody Branch branch) {
        return branchService.getBranchByCode(branchCode)
                .map(existing -> {
                    existing.setBranchName(branch.getBranchName());
                    existing.setLocation(branch.getLocation());
                    Branch updated = branchService.updateBranch(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // DELETE BRANCH
    // =========================
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteBranch(@RequestParam String branchCode) {
        Optional<Branch> branchOptional = branchService.getBranchByCode(branchCode);
        if (branchOptional.isPresent()) {
            branchService.deleteBranch(branchOptional.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}