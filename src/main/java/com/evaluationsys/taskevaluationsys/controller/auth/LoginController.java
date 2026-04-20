/*package com.evaluationsys.taskevaluationsys.controller.auth;

import com.evaluationsys.taskevaluationsys.dto.auth.LoginRequestDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.auth.LoginDTOResponse;
import com.evaluationsys.taskevaluationsys.service.auth.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginDTOResponse> login(@RequestBody LoginRequestDTO request) {
        try {
            LoginDTOResponse response = loginService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Could replace with custom exception handling
            return ResponseEntity.status(401).body(new LoginDTOResponse(null, null, e.getMessage()));
        }
    }
}

 */