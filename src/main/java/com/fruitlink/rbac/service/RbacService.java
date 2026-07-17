package com.fruitlink.rbac.service;

import com.fruitlink.common.BusinessException;
import com.fruitlink.rbac.dto.RbacDto.*;
import com.fruitlink.rbac.entity.Permission;
import com.fruitlink.rbac.entity.Role;
import com.fruitlink.rbac.entity.RolePermission;
import com.fruitlink.rbac.repository.PermissionRepository;
import com.fruitlink.rbac.repository.RolePermissionRepository;
import com.fruitlink.rbac.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream().map(this::toRoleResponse).toList();
    }

    public RoleResponse getRoleWithPermissions(String roleId) {
        Role role = roleRepository.findById(UUID.fromString(roleId))
                .orElseThrow(() -> new BusinessException("Role not found"));
        return toRoleResponse(role);
    }

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream().map(this::toPermissionResponse).toList();
    }

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest req) {
        permissionRepository.findByModuleAndAction(req.getModule(), req.getAction())
                .ifPresent(p -> { throw new BusinessException("Permission already exists"); });
        Permission p = new Permission();
        p.setModule(req.getModule());
        p.setAction(req.getAction());
        return toPermissionResponse(permissionRepository.save(p));
    }

    @Transactional
    public RoleResponse assignPermissions(String roleId, AssignPermissionsRequest req) {
        Role role = roleRepository.findById(UUID.fromString(roleId))
                .orElseThrow(() -> new BusinessException("Role not found"));

        for (String permId : req.getPermissionIds()) {
            Permission perm = permissionRepository.findById(UUID.fromString(permId))
                    .orElseThrow(() -> new BusinessException("Permission not found: " + permId));

            RolePermission.RolePermissionId id = new RolePermission.RolePermissionId();
            id.setRoleId(UUID.fromString(roleId));
            id.setPermissionId(UUID.fromString(permId));

            if (!rolePermissionRepository.existsById(id)) {
                RolePermission rp = new RolePermission();
                rp.setId(id);
                rp.setRole(role);
                rp.setPermission(perm);
                rolePermissionRepository.save(rp);
            }
        }
        return toRoleResponse(role);
    }

    @Transactional
    public void revokePermission(String roleId, String permissionId) {
        rolePermissionRepository.deleteByRoleIdAndPermissionId(
                UUID.fromString(roleId), UUID.fromString(permissionId));
    }

    private RoleResponse toRoleResponse(Role role) {
        RoleResponse r = new RoleResponse();
        r.setId(role.getId().toString());
        r.setName(role.getName());
        r.setPermissions(
                rolePermissionRepository.findByRoleId(role.getId()).stream()
                        .map(rp -> toPermissionResponse(rp.getPermission()))
                        .toList()
        );
        return r;
    }

    private PermissionResponse toPermissionResponse(Permission p) {
        PermissionResponse r = new PermissionResponse();
        r.setId(p.getId().toString());
        r.setModule(p.getModule());
        r.setAction(p.getAction());
        return r;
    }
}
