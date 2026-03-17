package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.entity.Branch;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    public Optional<Branch> getBranchById(Long branchId) {
        return branchRepository.findById(branchId);
    }

    public Optional<Branch> getBranchByCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode);
    }

    // CREATE
    public Branch createBranch(Branch branch) {

        String locationPrefix = getPrefix(branch.getLocation());
        String branchPrefix = getPrefix(branch.getBranchName());

        String prefix = locationPrefix + "/" + branchPrefix;

        int nextNumber = 1;

        Optional<Branch> lastBranch =
                branchRepository.findTopByBranchCodeStartingWithOrderByBranchCodeDesc(prefix);

        if (lastBranch.isPresent()) {
            String lastCode = lastBranch.get().getBranchCode(); // ACC/MAN/002
            String[] parts = lastCode.split("/");
            nextNumber = Integer.parseInt(parts[2]) + 1;
        }

        String newCode = prefix + "/" + String.format("%03d", nextNumber);
        branch.setBranchCode(newCode);

        return branchRepository.save(branch);
    }

    // UPDATE
    public Branch updateBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    // DELETE
    public void deleteBranch(Branch branch) {
        branchRepository.delete(branch);
    }

    // helper method
    private String getPrefix(String text) {
        if (text == null || text.isEmpty()) {
            return "XXX";
        }
        return text.substring(0, Math.min(3, text.length())).toUpperCase();
    }
}