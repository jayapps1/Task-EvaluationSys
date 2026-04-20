package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.SupervisorDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.SupervisorDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.repository.SupervisorRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SupervisorService {

    private final SupervisorRepository supervisorRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public SupervisorService(SupervisorRepository supervisorRepository,
                             DepartmentRepository departmentRepository,
                             BranchRepository branchRepository,
                             UserRepository userRepository) {
        this.supervisorRepository = supervisorRepository;
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // GET ALL
    // =========================
    public List<SupervisorDTOResponse> getAllSupervisors() {
        return supervisorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Optional<SupervisorDTOResponse> getSupervisorByCode(String supervisorCode) {
        return supervisorRepository.findBySupervisorCode(supervisorCode)
                .map(this::mapToResponse);
    }

    // =========================
    // CREATE
    // =========================
    @Transactional
    public SupervisorDTOResponse createSupervisorByCodes(SupervisorDTO dto) {

        Branch branch = branchRepository.findByBranchCode(dto.getBranchCode())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        Department department = departmentRepository.findByDepartmentCode(dto.getDepartmentCode())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        User staff = userRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (!department.getBranch().getBranchCode().equals(branch.getBranchCode())) {
            throw new RuntimeException("Department does not belong to branch");
        }

        supervisorRepository.findByUserAndBranchAndDepartment(staff, branch, department)
                .ifPresent(s -> {
                    throw new RuntimeException("Supervisor already exists");
                });

        Supervisor supervisor = new Supervisor();
        supervisor.setBranch(branch);
        supervisor.setDepartment(department);
        supervisor.setUser(staff);
        supervisor.setSupervisorCode(generateSupervisorCode(branch, department));

        return mapToResponse(supervisorRepository.save(supervisor));
    }

    // =========================
    // UPDATE
    // =========================
    @Transactional
    public Optional<SupervisorDTOResponse> updateSupervisorByCodes(String supervisorCode, SupervisorDTO dto) {

        return supervisorRepository.findBySupervisorCode(supervisorCode)
                .map(supervisor -> {

                    Branch branch = branchRepository.findByBranchCode(dto.getBranchCode())
                            .orElseThrow(() -> new RuntimeException("Branch not found"));

                    Department department = departmentRepository.findByDepartmentCode(dto.getDepartmentCode())
                            .orElseThrow(() -> new RuntimeException("Department not found"));

                    User staff = userRepository.findById(dto.getStaffId())
                            .orElseThrow(() -> new RuntimeException("Staff not found"));

                    if (!department.getBranch().getBranchCode().equals(branch.getBranchCode())) {
                        throw new RuntimeException("Department mismatch");
                    }

                    supervisorRepository.findByUserAndBranchAndDepartment(staff, branch, department)
                            .ifPresent(existing -> {
                                if (!existing.getSupervisorId().equals(supervisor.getSupervisorId())) {
                                    throw new RuntimeException("Duplicate supervisor exists");
                                }
                            });

                    supervisor.setBranch(branch);
                    supervisor.setDepartment(department);
                    supervisor.setUser(staff);

                    return mapToResponse(supervisorRepository.save(supervisor));
                });
    }

    // =========================
    // DELETE
    // =========================
    @Transactional
    public void deleteSupervisorByCode(String supervisorCode) {
        Supervisor supervisor = supervisorRepository.findBySupervisorCode(supervisorCode)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        supervisorRepository.delete(supervisor);
    }

    // =========================
    // CODE GENERATION
    // =========================
    private String generateSupervisorCode(Branch branch, Department department) {

        String branchCode = branch.getBranchName().length() >= 3
                ? branch.getBranchName().substring(0, 3).toUpperCase()
                : branch.getBranchName().toUpperCase();

        String deptCode = department.getDepartmentName().length() >= 3
                ? department.getDepartmentName().substring(0, 3).toUpperCase()
                : department.getDepartmentName().toUpperCase();

        String prefix = branchCode + "/" + deptCode + "/SUP/";

        Optional<Supervisor> last = supervisorRepository
                .findTopBySupervisorCodeStartingWithOrderBySupervisorCodeDesc(prefix);

        int next = last.map(s -> {
            try {
                return Integer.parseInt(s.getSupervisorCode().split("/")[3]) + 1;
            } catch (Exception e) {
                return 1;
            }
        }).orElse(1);

        return String.format("%s%03d", prefix, next);
    }

    // =========================
    // MAPPER
    // =========================
    private SupervisorDTOResponse mapToResponse(Supervisor supervisor) {

        SupervisorDTOResponse dto = new SupervisorDTOResponse();

        dto.setSupervisorId(supervisor.getSupervisorId());
        dto.setSupervisorCode(supervisor.getSupervisorCode());

        Branch branch = supervisor.getBranch();
        Department dept = supervisor.getDepartment();
        User user = supervisor.getUser();

        dto.setBranchId(branch != null ? branch.getBranchId() : null);
        dto.setBranchCode(branch != null ? branch.getBranchCode() : null);
        dto.setBranchName(branch != null ? branch.getBranchName() : null);

        dto.setDepartmentId(dept != null ? dept.getDepartmentId() : null);
        dto.setDepartmentCode(dept != null ? dept.getDepartmentCode() : null);
        dto.setDepartmentName(dept != null ? dept.getDepartmentName() : null);

        dto.setStaffId(user != null ? user.getStaffId() : null);
        dto.setStaffCode(user != null ? user.getStaffCode() : null);
        dto.setFirstName(user != null ? user.getFirstName() : null);
        dto.setOtherName(user != null ? user.getOtherName() : null);

        // 🔥 OPTIONAL: if you added these in DTO
        dto.setSupervisorPhone(user != null ? user.getPhoneNumber() : null);

        dto.setCreatedAt(supervisor.getCreatedAt());
        dto.setUpdatedAt(supervisor.getUpdatedAt());

        return dto;
    }

    // =========================
    // FIXED SUPERVISOR LOOKUP (NO INVALID SPRING METHOD)
    // =========================
    public Optional<Supervisor> findDepartmentSupervisor(User staff) {

        if (staff == null || staff.getDepartment() == null) {
            return Optional.empty();
        }

        // ✅ FIX: use valid repository method
        return supervisorRepository.findByUserStaffCode(staff.getStaffCode());
    }

    // =========================
    // CONTACT FOR UI
    // =========================
    public Map<String, String> getSupervisorContact(User staff) {

        Map<String, String> result = new HashMap<>();

        Supervisor supervisor = findDepartmentSupervisor(staff).orElse(null);

        if (supervisor != null && supervisor.getUser() != null) {

            User u = supervisor.getUser();

            result.put("name",
                    (u.getFirstName() != null ? u.getFirstName() : "") + " " +
                            (u.getOtherName() != null ? u.getOtherName() : "")
            );

            result.put("phone",
                    u.getPhoneNumber() != null ? u.getPhoneNumber() : "N/A"
            );

        } else {
            result.put("name", "N/A");
            result.put("phone", "N/A");
        }

        return result;
    }
}