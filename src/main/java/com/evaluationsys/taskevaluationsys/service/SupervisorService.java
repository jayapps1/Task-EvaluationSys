package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.SupervisorDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.SupervisorDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Department;
import com.evaluationsys.taskevaluationsys.entity.Supervisor;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupervisorService {

    private final SupervisorRepository supervisorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public SupervisorService(SupervisorRepository supervisorRepository,
                             DepartmentRepository departmentRepository,
                             UserRepository userRepository) {
        this.supervisorRepository = supervisorRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    // GET ALL SUPERVISORS
    public List<SupervisorDTOResponse> getAllSupervisors() {
        return supervisorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET SUPERVISOR BY CODE
    public Optional<SupervisorDTOResponse> getSupervisorByCode(String supervisorCode) {
        return supervisorRepository.findBySupervisorCode(supervisorCode)
                .map(this::mapToResponse);
    }

    private String generateSupervisorCode(Supervisor supervisor) {
        String branchName = supervisor.getDepartment().getBranch().getBranchName();
        String deptName = supervisor.getDepartment().getDepartmentName();

        String branchCode = branchName.length() >= 3 ? branchName.substring(0,3).toUpperCase() : branchName.toUpperCase();
        String deptCode = deptName.length() >= 3 ? deptName.substring(0,3).toUpperCase() : deptName.toUpperCase();

        // Get last supervisor for sequence
        String prefix = branchCode + "/" + deptCode + "/SUP/";
        Optional<Supervisor> lastSupervisor =
                supervisorRepository.findTopBySupervisorCodeStartingWithOrderBySupervisorCodeDesc(prefix);

        int nextNumber = 1;
        if (lastSupervisor.isPresent()) {
            String lastCode = lastSupervisor.get().getSupervisorCode();
            String[] parts = lastCode.split("/");
            nextNumber = Integer.parseInt(parts[3]) + 1; // last ### + 1
        }

        return String.format("%s%03d", prefix, nextNumber);
    }

    // CREATE SUPERVISOR
    @Transactional
    public SupervisorDTOResponse createSupervisor(SupervisorDTO dto) {

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Supervisor supervisor = new Supervisor();
        supervisor.setDepartment(department);
        supervisor.setUser(user);

        String supervisorCode = generateSupervisorCode(supervisor);
        supervisor.setSupervisorCode(supervisorCode);

        Supervisor savedSupervisor = supervisorRepository.save(supervisor);

        return mapToResponse(savedSupervisor);
    }

    // UPDATE SUPERVISOR
    @Transactional
    public Optional<SupervisorDTOResponse> updateSupervisor(String supervisorCode, SupervisorDTO dto) {

        return supervisorRepository.findBySupervisorCode(supervisorCode)
                .map(supervisor -> {

                    Department department = departmentRepository.findById(dto.getDepartmentId())
                            .orElseThrow(() -> new RuntimeException("Department not found"));

                    User user = userRepository.findById(dto.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    supervisor.setDepartment(department);
                    supervisor.setUser(user);

                    Supervisor updatedSupervisor = supervisorRepository.save(supervisor);

                    return mapToResponse(updatedSupervisor);
                });
    }

    // DELETE SUPERVISOR
    @Transactional
    public void deleteSupervisor(String supervisorCode) {

        supervisorRepository.findBySupervisorCode(supervisorCode)
                .ifPresent(supervisorRepository::delete);
    }

    // ENTITY → RESPONSE DTO
    private SupervisorDTOResponse mapToResponse(Supervisor supervisor) {

        SupervisorDTOResponse dto = new SupervisorDTOResponse();

        dto.setSupervisorId(supervisor.getSupervisorId());
        dto.setSupervisorCode(supervisor.getSupervisorCode());

        dto.setDepartmentId(supervisor.getDepartment().getDepartmentId());
        dto.setDepartmentName(supervisor.getDepartment().getDepartmentName());

        dto.setBranchId(supervisor.getDepartment().getBranch().getBranchId());
        dto.setBranchName(supervisor.getDepartment().getBranch().getBranchName());

        dto.setStaffId(supervisor.getUser().getStaffId());
        dto.setStaffCode(supervisor.getUser().getStaffCode());
        dto.setFirstName(supervisor.getUser().getFirstName());
        dto.setOtherName(supervisor.getUser().getOtherName());

        dto.setCreatedAt(supervisor.getCreatedAt());
        dto.setUpdatedAt(supervisor.getUpdatedAt());

        return dto;
    }
}