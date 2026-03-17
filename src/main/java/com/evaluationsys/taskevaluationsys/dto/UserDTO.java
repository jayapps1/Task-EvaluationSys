package com.evaluationsys.taskevaluationsys.dto;

import com.evaluationsys.taskevaluationsys.entity.Role;

public class UserDTO {

    private Long staffId;
    private Long staffCode;
    private String firstName;
    private String otherName;
    private String email;
    private String phoneNumber;
    private Role role;
    private String rank;
    private Long departmentId;
    private Long branchId;

    public UserDTO() {}


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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }


}