package com.evaluationsys.taskevaluationsys.dto.auth;

public class LoginRequestDTO {
    private Long staffCode;
    private String password;

    // Getters and setters
    public Long getStaffCode() { return staffCode; }
    public void setStaffCode(Long staffCode) { this.staffCode = staffCode; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}