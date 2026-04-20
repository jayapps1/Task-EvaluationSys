package com.evaluationsys.taskevaluationsys.dto;

public class DepartmentDTO {
    private Long departmentId;
    private String departmentName;
    private String branchName;
    private String branchLocation;

    public DepartmentDTO(Long departmentId, String departmentName, String branchName, String branchLocation) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.branchName = branchName;
        this.branchLocation = branchLocation;
    }

    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getBranchName() { return branchName; }
    public String getBranchLocation() { return branchLocation; }
}