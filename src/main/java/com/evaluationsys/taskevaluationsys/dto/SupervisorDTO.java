package com.evaluationsys.taskevaluationsys.dto;

public class SupervisorDTO {

    private String branchCode;
    private String departmentCode;
    private String supervisorCode; // main identifier only

    // Add this field to identify the staff/user by internal ID
    private Long staffId;

    // =====================
    // Getters and Setters
    // =====================
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String getSupervisorCode() { return supervisorCode; }
    public void setSupervisorCode(String supervisorCode) { this.supervisorCode = supervisorCode; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
}