package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.entity.Branch;
import com.evaluationsys.taskevaluationsys.entity.Department;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             BranchRepository branchRepository) {
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
    }

    // =========================
    // GET ALL
    // =========================
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // =========================
    // GET BY CODE
    // =========================
    public Optional<Department> getDepartmentByCode(String departmentCode) {
        return departmentRepository.findByDepartmentCode(departmentCode);
    }

    // =========================
    // CREATE
    // =========================
    public Department createDepartment(Department department) {

        String name = department.getDepartmentName().toUpperCase();
        String prefix = name.substring(0, Math.min(3, name.length()));
        long count = departmentRepository.count();

        String code = prefix + String.format("%03d", count + 1);
        department.setDepartmentCode(code);


        if (department.getBranch() != null &&
                department.getBranch().getBranchCode() != null) {

            Branch branch = branchRepository
                    .findByBranchCode(department.getBranch().getBranchCode())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));

            department.setBranch(branch);
        }

        return departmentRepository.save(department);
    }

    // =========================
    // UPDATE
    // =========================
    public Optional<Department> updateDepartment(String departmentCode, Department departmentDetails) {

        return departmentRepository.findByDepartmentCode(departmentCode)
                .map(department -> {

                    department.setDepartmentName(departmentDetails.getDepartmentName());

                    //  attach REAL branch
                    if (departmentDetails.getBranch() != null &&
                            departmentDetails.getBranch().getBranchCode() != null) {

                        Branch branch = branchRepository
                                .findByBranchCode(departmentDetails.getBranch().getBranchCode())
                                .orElseThrow(() -> new RuntimeException("Branch not found"));

                        department.setBranch(branch);
                    }

                    return departmentRepository.save(department);
                });
    }

    // =========================
    // DELETE
    // =========================
    public void deleteDepartment(String departmentCode) {

        departmentRepository.findByDepartmentCode(departmentCode)
                .ifPresent(departmentRepository::delete);
    }
}