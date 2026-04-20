package com.evaluationsys.taskevaluationsys.controller;

import com.evaluationsys.taskevaluationsys.dto.UserDTO;
import com.evaluationsys.taskevaluationsys.dtoresponse.UserDTOResponse;
import com.evaluationsys.taskevaluationsys.entity.enums.Role;
import com.evaluationsys.taskevaluationsys.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET ALL USERS
    @GetMapping("/fetchAllUsers")
    public ResponseEntity<List<UserDTOResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET USER BY STAFF CODE
    @GetMapping("/fetchUser/{staffCode}")
    public ResponseEntity<UserDTOResponse> getUserByStaffCode(@PathVariable Long staffCode) {
        Optional<UserDTOResponse> user = userService.getUserByStaffCode(staffCode);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // SEARCH USERS BY NAME
    @GetMapping("/search")
    public ResponseEntity<List<UserDTOResponse>> searchUsers(@RequestParam("q") String q) {
        return ResponseEntity.ok(userService.searchUsersByName(q));
    }

    // CREATE USER
    @PostMapping("/createUser")
    public ResponseEntity<UserDTOResponse> createUser(@RequestBody UserDTO userDTO) {
        UserDTOResponse savedUser = userService.createUser(userDTO);
        return ResponseEntity
                .created(URI.create("/users/fetchUser/" + savedUser.getStaffCode()))
                .body(savedUser);
    }

    // UPDATE USER (Full update)
    @PutMapping("/updateUser/{staffCode}")
    public ResponseEntity<UserDTOResponse> updateUser(
            @PathVariable Long staffCode,
            @RequestBody UserDTO userDTO) {

        Optional<UserDTOResponse> updatedUser = userService.updateUserByStaffCode(staffCode, userDTO);
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /users/updateRoleOnly/{staffCode}?role=SUPERVISOR
    @PutMapping("/updateRoleOnly/{staffCode}")
    public ResponseEntity<Void> updateUserRoleOnly(
            @PathVariable Long staffCode,
            @RequestParam Role role) {

        boolean updated = userService.updateUserRoleOnly(staffCode, role);
        if (updated) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // Staff code not found
        }
    }

    // DELETE USER
    @DeleteMapping("/deleteUser/{staffCode}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long staffCode) {
        userService.deleteUserByStaffCode(staffCode);
        return ResponseEntity.noContent().build();
    }
}