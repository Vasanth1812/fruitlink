package com.fruitlink.users.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.users.dto.UserDto.UserResponse;
import com.fruitlink.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user management")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@RequestParam(required = false) String role) {
        if (role == null || role.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(List.of())); // or throw error
        }
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
