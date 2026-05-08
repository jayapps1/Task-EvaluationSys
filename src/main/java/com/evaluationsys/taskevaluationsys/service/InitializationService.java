package com.evaluationsys.taskevaluationsys.service;

import com.evaluationsys.taskevaluationsys.entity.Branch;
import com.evaluationsys.taskevaluationsys.entity.Department;
import com.evaluationsys.taskevaluationsys.entity.User;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.repository.BranchRepository;
import com.evaluationsys.taskevaluationsys.repository.DepartmentRepository;
import com.evaluationsys.taskevaluationsys.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InitializationService {

    private static final Logger logger = LoggerFactory.getLogger(InitializationService.class);

    private final BranchService branchService;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Optional: Make admin credentials configurable
    @Value("${app.admin.staff-code:1001}")
    private Long adminStaffCode;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.email:admin@system.com}")
    private String adminEmail;

    public InitializationService(BranchService branchService,
                                 BranchRepository branchRepository,
                                 DepartmentRepository departmentRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.branchService = branchService;
        this.branchRepository = branchRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initializeDefaultData() {
        logger.info("========================================");
        logger.info("Starting data initialization...");
        logger.info("========================================");

        initializeDefaultBranch();
        initializeDefaultDepartment();
        initializeDefaultAdmin();

        logger.info("========================================");
        logger.info("Data initialization completed!");
        logger.info("========================================");
    }

    private void initializeDefaultBranch() {
        if (branchRepository.count() == 0) {
            logger.info("No branches found. Creating default branch...");

            Branch branch = new Branch();
            branch.setBranchName("Head Office");
            branch.setLocation("Accra");

            // Let BranchService generate the branch code automatically
            Branch savedBranch = branchService.createBranch(branch);

            logger.info("✅ Default branch created successfully!");
            logger.info("   Branch Name: {}", savedBranch.getBranchName());
            logger.info("   Branch Code: {}", savedBranch.getBranchCode());
            logger.info("   Location: {}", savedBranch.getLocation());
        } else {
            logger.info("Branches already exist ({} found). Skipping default branch creation.",
                    branchRepository.count());
        }
    }

    private void initializeDefaultDepartment() {
        if (departmentRepository.count() == 0) {
            logger.info("No departments found. Creating default department...");

            // Find the default branch (Head Office) or any branch
            Branch defaultBranch = branchRepository.findAll().stream()
                    .findFirst()
                    .orElse(null);

            if (defaultBranch == null) {
                logger.warn("Cannot create department: No branch found. Please create a branch first.");
                return;
            }

            Department department = new Department();
            department.setDepartmentCode("IT");
            department.setDepartmentName("Information Technology");
            department.setBranch(defaultBranch);

            Department savedDepartment = departmentRepository.save(department);

            logger.info("✅ Default department created successfully!");
            logger.info("   Department Name: {}", savedDepartment.getDepartmentName());
            logger.info("   Department Code: {}", savedDepartment.getDepartmentCode());
            logger.info("   Branch: {}", defaultBranch.getBranchName());
        } else {
            logger.info("Departments already exist ({} found). Skipping default department creation.",
                    departmentRepository.count());
        }
    }

    private void initializeDefaultAdmin() {
        // Check if any admin exists using your repository method
        long adminCount = userRepository.countByRole(Role.ADMIN);

        if (adminCount == 0) {
            logger.info("No admin user found. Creating default admin user...");

            User admin = new User();
            admin.setStaffCode(adminStaffCode);
            admin.setFirstName("System");
            admin.setOtherName("Administrator");
            admin.setEmail(adminEmail);
            admin.setPhoneNumber("+233000000000");
            admin.setRole(Role.ADMIN);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setActive(true);
            admin.setRank("Senior Administrator");

            // Assign default branch if available
            branchRepository.findAll().stream()
                    .findFirst()
                    .ifPresent(admin::setBranch);

            // Assign default department if available
            departmentRepository.findAll().stream()
                    .findFirst()
                    .ifPresent(admin::setDepartment);

            userRepository.save(admin);

            printAdminCredentials();
        } else {
            logger.info("Admin user(s) already exist ({} found). Skipping default admin creation.", adminCount);
        }
    }

    private void printAdminCredentials() {
        String separator = "========================================";

        logger.info(separator);
        logger.info("✅ DEFAULT ADMIN USER CREATED!");
        logger.info(separator);
        logger.info("   Staff Code: {}", adminStaffCode);
        logger.info("   Password: {}", adminPassword);
        logger.info("   Email: {}", adminEmail);
        logger.info("   Role: ADMIN");
        logger.info(separator);
        logger.info("⚠️  IMPORTANT: Change this password after first login!");
        logger.info(separator);

        // Also print to console for better visibility
        System.out.println("\n\n");
        System.out.println(separator);
        System.out.println("✅ DEFAULT ADMIN USER CREATED!");
        System.out.println(separator);
        System.out.println("   Staff Code: " + adminStaffCode);
        System.out.println("   Password: " + adminPassword);
        System.out.println("   Email: " + adminEmail);
        System.out.println("   Role: ADMIN");
        System.out.println(separator);
        System.out.println("⚠️  IMPORTANT: Change this password after first login!");
        System.out.println(separator);
        System.out.println("\n\n");
    }
}