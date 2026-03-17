package com.evaluationsys.taskevaluationsys.dtoresponse;

public class UserDTOResponse {

    private Long staffId;
    private Long staffCode;
    private String firstName;
    private String otherName;
    private String email;
    private String phoneNumber;
    private String role;
    private String rank;
    private String departmentName;
    private String branchName;
    private Boolean active;



    public Long getStaffId() {
        return staffId;
    }
    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getStaffCode() {return staffCode;}
    public void setStaffCode(Long staffCode) {this.staffCode = staffCode;}

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getOtherName() {
        return otherName;
    }
    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


}