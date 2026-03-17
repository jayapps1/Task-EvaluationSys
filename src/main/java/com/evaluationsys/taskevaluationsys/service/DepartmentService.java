package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.entity.Department;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // =========================
    // GET ALL DEPARTMENTS
    // =========================
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // =========================
    // GET DEPARTMENT BY CODE
    // =========================
    public Optional<Department> getDepartmentByCode(String departmentCode) {

        return departmentRepository.findByDepartmentCode(departmentCode);

    }

    // =========================
    // CREATE DEPARTMENT
    // =========================
    public Department createDepartment(Department department) {

        // convert name to uppercase
        String name = department.getDepartmentName().toUpperCase();

        // create prefix (first 3 letters)
        String prefix = name.substring(0, Math.min(3, name.length()));

        // count departments to generate number
        long count = departmentRepository.count();

        // create code
        String code = prefix + String.format("%03d", count + 1);

        department.setDepartmentCode(code);

        return departmentRepository.save(department);
    }

    // =========================
    // UPDATE DEPARTMENT BY CODE
    // =========================
    public Optional<Department> updateDepartment(String departmentCode, Department departmentDetails) {

        return departmentRepository.findByDepartmentCode(departmentCode)
                .map(department -> {

                    department.setDepartmentName(departmentDetails.getDepartmentName());
                    department.setBranch(departmentDetails.getBranch());

                    return departmentRepository.save(department);

                });
    }

    // =========================
    // DELETE DEPARTMENT BY CODE
    // =========================
    public void deleteDepartment(String departmentCode) {

        departmentRepository.findByDepartmentCode(departmentCode)
                .ifPresent(departmentRepository::delete);

    }

}