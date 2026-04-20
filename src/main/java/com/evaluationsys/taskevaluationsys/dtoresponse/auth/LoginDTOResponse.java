package com.evaluationsys.taskevaluationsys.dtoresponse.auth;

public class LoginDTOResponse {
    private String token;
    private String role;
    private String firstName;

    public LoginDTOResponse(String token, String role, String firstName) {
        this.token = token;
        this.role = role;
        this.firstName = firstName;
    }

    // Getters
    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getFirstName() { return firstName; }
}