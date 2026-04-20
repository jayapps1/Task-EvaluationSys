package com.evaluationsys.taskevaluationsys.controller.auth;

import com.evaluationsys.taskevaluationsys.dto.DepartmentDTO;
import com.evaluationsys.taskevaluationsys.dto.auth.RegisterDTO;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.service.auth.RegisterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/auth")
public class RegisterFormController {

    private final RegisterService registerService;
    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;

    public RegisterFormController(RegisterService registerService,
                                  DepartmentRepository departmentRepository,
                                  BranchRepository branchRepository) {
        this.registerService = registerService;
        this.departmentRepository = departmentRepository;
        this.branchRepository = branchRepository;
    }

    // ✅ ONLY ADMIN CAN ACCESS PAGE
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/register")
    public String showRegisterForm(Model model) {

        // Map departments to DepartmentDTO including branch info
        List<DepartmentDTO> departmentsWithBranch = departmentRepository.findAll().stream()
                .map(d -> new DepartmentDTO(
                        d.getDepartmentId(),
                        d.getDepartmentName(),
                        d.getBranch().getBranchName(),
                        d.getBranch().getLocation()
                ))
                .collect(Collectors.toList());

        model.addAttribute("departments", departmentsWithBranch);
        model.addAttribute("branches", branchRepository.findAll());
        model.addAttribute("registerDTO", new RegisterDTO());

        return "layout/register";
    }

    // ✅ ONLY ADMIN CAN SUBMIT
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterDTO registerDTO, Model model) {

        try {
            // 🔍 DEBUG
            System.out.println("Registering user: " + registerDTO.getStaffCode());

            registerService.registerUser(registerDTO);

            // ✅ CORRECT REDIRECT
            return "redirect:/admin/dashboard?userCreated=true";

        } catch (Exception e) {

            e.printStackTrace();

            // Return form with error message
            List<DepartmentDTO> departmentsWithBranch = departmentRepository.findAll().stream()
                    .map(d -> new DepartmentDTO(
                            d.getDepartmentId(),
                            d.getDepartmentName(),
                            d.getBranch().getBranchName(),
                            d.getBranch().getLocation()
                    ))
                    .collect(Collectors.toList());

            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", departmentsWithBranch);
            model.addAttribute("branches", branchRepository.findAll());
            model.addAttribute("registerDTO", registerDTO);

            return "layout/register";
        }
    }
}