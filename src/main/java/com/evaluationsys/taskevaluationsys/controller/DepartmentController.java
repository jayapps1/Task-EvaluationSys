package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.entity.Department;
import com.evaluationsys.taskevaluationsys.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // =========================
    // GET ALL DEPARTMENTS
    // =========================
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {

        return ResponseEntity.ok(departmentService.getAllDepartments());

    }

    // =========================
    // GET DEPARTMENT BY CODE
    // =========================
    @GetMapping("/code/{departmentCode}")
    public ResponseEntity<Department> getDepartmentByCode(@PathVariable String departmentCode) {

        return departmentService.getDepartmentByCode(departmentCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    // =========================
    // CREATE DEPARTMENT
    // =========================
    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {

        Department created = departmentService.createDepartment(department);

        return ResponseEntity.ok(created);

    }

    // =========================
    // UPDATE DEPARTMENT
    // =========================
    @PutMapping("/code/{departmentCode}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable String departmentCode,
            @RequestBody Department departmentDetails) {

        return departmentService.updateDepartment(departmentCode, departmentDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    // =========================
    // DELETE DEPARTMENT
    // =========================
    @DeleteMapping("/code/{departmentCode}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String departmentCode) {

        departmentService.deleteDepartment(departmentCode);

        return ResponseEntity.noContent().build();

    }

}