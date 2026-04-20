package com.evaluationsys.taskevaluationsys.service.auth;

import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.dto.auth.RegisterDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.auth.RegisterDTOResponse;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           BranchRepository branchRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterDTOResponse registerUser(RegisterDTO dto) {

        // Check duplicates
        if (userRepository.existsByStaffCode(dto.getStaffCode())) {
            throw new RuntimeException("StaffCode already exists: " + dto.getStaffCode());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already in use: " + dto.getEmail());
        }

        // Build User
        User user = new User();
        user.setStaffCode(dto.getStaffCode());
        user.setFirstName(dto.getFirstName());
        user.setOtherName(dto.getOtherName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRank(dto.getRank());

        // Map role
        if (dto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + dto.getRole());
            }
        }

        // Department
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            user.setDepartment(department);
        }

        // Branch
        if (dto.getBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            user.setBranch(branch);
        }

        // Default password and active
        user.setPasswordHash(passwordEncoder.encode("default123"));
        user.setActive(true);

        // Save User
        User saved = userRepository.save(user);

        // Map entity → DTOResponse
        RegisterDTOResponse response = new RegisterDTOResponse();
        response.setStaffId(saved.getStaffId());
        response.setStaffCode(saved.getStaffCode());
        response.setFirstName(saved.getFirstName());
        response.setOtherName(saved.getOtherName());
        response.setEmail(saved.getEmail());
        response.setPhoneNumber(saved.getPhoneNumber());
        response.setRank(saved.getRank());
        response.setDepartmentName(saved.getDepartment() != null ? saved.getDepartment().getDepartmentName() : null);
        response.setBranchName(saved.getBranch() != null ? saved.getBranch().getBranchName() : null);
        response.setRole(saved.getRole() != null ? saved.getRole().name() : null);
        response.setActive(saved.getActive());

        return response;
    }
}