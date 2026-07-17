package com.fruitlink.rbac.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.rbac.dto.RbacDto.*;
import com.fruitlink.rbac.service.RbacService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rbac")
@RequiredArgsConstructor
@Tag(name = "RBAC", description = "Roles and Permissions management")
public class RbacController {

    private final RbacService rbacService;

    // ── Roles ──────────────────────────────────────────────

    @GetMapping("/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getAllRoles()));
    }

    @GetMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable String roleId) {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getRoleWithPermissions(roleId)));
    }

    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @PathVariable String roleId,
            @RequestBody AssignPermissionsRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned", rbacService.assignPermissions(roleId, req)));
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable String roleId,
            @PathVariable String permissionId) {
        rbacService.revokePermission(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission revoked", null));
    }

    // ── Permissions ────────────────────────────────────────

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getAllPermissions()));
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @RequestBody CreatePermissionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission created", rbacService.createPermission(req)));
    }
}
