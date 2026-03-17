package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.dto.UserDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.UserDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.Branch;
import com.evaluationsys.taskevaluationsys.entity.Department;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
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

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       BranchRepository branchRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
    }

    // Convert User Entity → Response DTO
    private UserDTOResponse toResponse(User user) {
        UserDTOResponse dto = new UserDTOResponse();

        dto.setStaffId(user.getStaffId());
        dto.setStaffCode(user.getStaffCode());
        dto.setFirstName(user.getFirstName());
        dto.setOtherName(user.getOtherName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setRank(user.getRank());
        dto.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null);
        dto.setBranchName(user.getBranch() != null ? user.getBranch().getBranchName() : null);
        dto.setActive(user.getActive());

        return dto;
    }

    // GET ALL USERS
    public List<UserDTOResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET USER BY STAFF CODE
    public Optional<UserDTOResponse> getUserByStaffCode(Long staffCode) {
        return userRepository.findByStaffCode(staffCode)
                .map(this::toResponse);
    }

    // CREATE USER
    @Transactional
    public UserDTOResponse createUser(UserDTO userDTO) {

        if (userRepository.existsByStaffCode(userDTO.getStaffCode())) {
            throw new RuntimeException("StaffCode already exists: " + userDTO.getStaffCode());
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
        user.setRole(userDTO.getRole());
        user.setRank(userDTO.getRank());

        if (userDTO.getDepartmentId() != null) {
            Department department = departmentRepository
                    .findById(userDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + userDTO.getDepartmentId()));
            user.setDepartment(department);
        }

        if (userDTO.getBranchId() != null) {
            Branch branch = branchRepository
                    .findById(userDTO.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + userDTO.getBranchId()));
            user.setBranch(branch);
        }

        user.setPasswordHash("default123");
        user.setActive(true);

        return toResponse(userRepository.save(user));
    }

    // UPDATE USER BY STAFF CODE
    @Transactional
    public Optional<UserDTOResponse> updateUserByStaffCode(Long staffCode, UserDTO userDTO) {

        return userRepository.findByStaffCode(staffCode).map(user -> {

            if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(userDTO.getEmail())) {
                    throw new RuntimeException("Email already in use: " + userDTO.getEmail());
                }
                user.setEmail(userDTO.getEmail());
            }

            user.setFirstName(userDTO.getFirstName());
            user.setOtherName(userDTO.getOtherName());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setRole(userDTO.getRole());
            user.setRank(userDTO.getRank());

            if (userDTO.getDepartmentId() != null) {
                Department department = departmentRepository
                        .findById(userDTO.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found with id: " + userDTO.getDepartmentId()));
                user.setDepartment(department);
            }

            if (userDTO.getBranchId() != null) {
                Branch branch = branchRepository
                        .findById(userDTO.getBranchId())
                        .orElseThrow(() -> new RuntimeException("Branch not found with id: " + userDTO.getBranchId()));
                user.setBranch(branch);
            }

            return toResponse(userRepository.save(user));
        });
    }

    // DELETE USER BY STAFF CODE
    @Transactional
    public void deleteUserByStaffCode(Long staffCode) {
        userRepository.findByStaffCode(staffCode).ifPresent(userRepository::delete);
    }
}