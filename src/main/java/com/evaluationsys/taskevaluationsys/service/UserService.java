package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.UserDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.UserDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.*;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       BranchRepository branchRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // Convert User entity → Response DTO
    // =========================
    private UserDTOResponse toResponse(User user) {
        if (user == null) return null;

        UserDTOResponse dto = new UserDTOResponse();
        dto.setStaffId(user.getStaffId());
        dto.setStaffCode(user.getStaffCode());
        dto.setFirstName(user.getFirstName());
        dto.setOtherName(user.getOtherName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null); // enum to string
        dto.setRank(user.getRank());
        dto.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null);
        dto.setBranchName(user.getBranch() != null ? user.getBranch().getBranchName() : null);
        dto.setActive(user.getActive());

        return dto;
    }

    // =========================
    // Get all users
    // =========================
    public List<UserDTOResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // Get user by staff code
    // =========================
    public Optional<UserDTOResponse> getUserByStaffCode(Long staffCode) {
        return userRepository.findByStaffCode(staffCode)
                .map(this::toResponse);
    }

    // =========================
    // Search users by firstName or otherName
    // =========================
    public List<UserDTOResponse> searchUsersByName(String query) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrOtherNameContainingIgnoreCase(query, query)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // Create new user
    // =========================
    @Transactional
    public UserDTOResponse createUser(UserDTO userDTO) {
        if (userDTO == null) throw new IllegalArgumentException("UserDTO cannot be null");

        if (userRepository.existsByStaffCode(userDTO.getStaffCode())) {
            throw new RuntimeException("Staff code already exists: " + userDTO.getStaffCode());
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already in use: " + userDTO.getEmail());
        }

        User user = new User();
        user.setStaffCode(userDTO.getStaffCode());
        user.setFirstName(userDTO.getFirstName());
        user.setOtherName(userDTO.getOtherName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole()); // enum
        user.setRank(userDTO.getRank());
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode("default123")); // default password

        // Set Department
        if (userDTO.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(userDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + userDTO.getDepartmentId()));
            user.setDepartment(dept);
        }

        // Set Branch
        if (userDTO.getBranchId() != null) {
            Branch branch = branchRepository.findById(userDTO.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + userDTO.getBranchId()));
            user.setBranch(branch);
        }

        return toResponse(userRepository.save(user));
    }

    // =========================
    // Update user by staff code
    // =========================
    @Transactional
    public Optional<UserDTOResponse> updateUserByStaffCode(Long staffCode, UserDTO userDTO) {
        return userRepository.findByStaffCode(staffCode)
                .map(user -> {

                    // Update simple fields
                    if (userDTO.getFirstName() != null) user.setFirstName(userDTO.getFirstName());
                    if (userDTO.getOtherName() != null) user.setOtherName(userDTO.getOtherName());
                    if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
                    if (userDTO.getRank() != null) user.setRank(userDTO.getRank());

                    // Update role (enum) safely
                    if (userDTO.getRole() != null) user.setRole(userDTO.getRole());

                    // Update email with uniqueness check
                    if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
                        if (userRepository.existsByEmail(userDTO.getEmail())) {
                            throw new RuntimeException("Email already in use: " + userDTO.getEmail());
                        }
                        user.setEmail(userDTO.getEmail());
                    }

                    // Update Department
                    if (userDTO.getDepartmentId() != null) {
                        Department dept = departmentRepository.findById(userDTO.getDepartmentId())
                                .orElseThrow(() -> new RuntimeException("Department not found with id: " + userDTO.getDepartmentId()));
                        user.setDepartment(dept);
                    }

                    // Update Branch
                    if (userDTO.getBranchId() != null) {
                        Branch branch = branchRepository.findById(userDTO.getBranchId())
                                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + userDTO.getBranchId()));
                        user.setBranch(branch);
                    }

                    return toResponse(userRepository.save(user));
                });
    }
    // =========================
// Update role only (enum-safe)
// =========================
    @Transactional
    public Optional<UserDTOResponse> updateUserRole(Long staffCode, Role role) {
        return userRepository.findByStaffCode(staffCode)
                .map(user -> {
                    user.setRole(role); // Enum assignment
                    User savedUser = userRepository.save(user);
                    return toResponse(savedUser); // Use existing method
                });
    }
    @Transactional
    public boolean updateUserRoleOnly(Long staffCode, Role role) {
        int updated = userRepository.updateRoleByStaffCode(staffCode, role);
        return updated > 0; // returns true if role was updated
    }
    // =========================
    // Delete user by staff code
    // =========================
    @Transactional
    public void deleteUserByStaffCode(Long staffCode) {
        userRepository.findByStaffCode(staffCode).ifPresent(userRepository::delete);
    }
}