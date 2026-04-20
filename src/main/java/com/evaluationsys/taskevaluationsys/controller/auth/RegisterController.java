package com.evaluationsys.taskevaluationsys.controller.auth;

import com.evaluationsys.taskevaluationsys.dto.auth.RegisterDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.auth.RegisterDTOResponse;
import com.evaluationsys.taskevaluationsys.service.auth.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// For REST API
@RestController
@RequestMapping("/api/auth") // <--- different prefix
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping("/register") // /api/auth/register
    public ResponseEntity<RegisterDTOResponse> register(@RequestBody RegisterDTO dto) {
        RegisterDTOResponse response = registerService.registerUser(dto);
        return ResponseEntity.ok(response);
    }
}