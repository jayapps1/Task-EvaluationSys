package com.evaluationsys.taskevaluationsys.dto;

public class SupervisorDTO {

    private String branchCode;
    private String branchName;
    private String departmentCode;
    private String departmentName;
    private String supervisorCode;
    private Long staffId;
    private String firstName;
    private String otherName;
    // =====================
    // Constructors
    // =====================
    public SupervisorDTO() {}

    public SupervisorDTO(String branchCode, String branchName, String departmentCode,
                         String departmentName, String supervisorCode, Long staffId) {
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.supervisorCode = supervisorCode;
        this.staffId = staffId;
    }

    // =====================
    // Getters and Setters
    // =====================
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getSupervisorCode() { return supervisorCode; }
    public void setSupervisorCode(String supervisorCode) { this.supervisorCode = supervisorCode; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getOtherName() { return otherName; }
    public void setOtherName(String otherName) { this.otherName = otherName; }
}