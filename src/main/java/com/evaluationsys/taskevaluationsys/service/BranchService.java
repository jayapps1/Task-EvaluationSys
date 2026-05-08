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

    // CREATE - Modernized to generate numeric codes
    public Branch createBranch(Branch branch) {

        String locationPrefix = getPrefix(branch.getLocation());
        String branchPrefix = getPrefix(branch.getBranchName());

        // Convert letter prefixes to numbers (A=1, B=2, etc.)
        String locationNum = convertToNumbers(locationPrefix);
        String branchNum = convertToNumbers(branchPrefix);

        String prefix = locationNum + branchNum;

        int nextNumber = 1;

        Optional<Branch> lastBranch =
                branchRepository.findTopByBranchCodeStartingWithOrderByBranchCodeDesc(prefix);

        if (lastBranch.isPresent()) {
            String lastCode = lastBranch.get().getBranchCode(); // e.g., "120103001"
            // Extract the sequence part (last 4 digits)
            String sequencePart = lastCode.substring(prefix.length());
            nextNumber = Integer.parseInt(sequencePart) + 1;
        }

        // Generate code: LocationNum(6) + BranchNum(6) + Sequence(4)
        String newCode = prefix + String.format("%04d", nextNumber);
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

    // Convert letters to numbers: A=01, B=02, etc.
    private String convertToNumbers(String text) {
        StringBuilder numbers = new StringBuilder();
        for (char c : text.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                numbers.append(String.format("%02d", c - 'A' + 1));
            }
        }
        return numbers.toString();
    }

    // helper method
    private String getPrefix(String text) {
        if (text == null || text.isEmpty()) {
            return "XXX";
        }
        return text.substring(0, Math.min(3, text.length())).toUpperCase();
    }
}