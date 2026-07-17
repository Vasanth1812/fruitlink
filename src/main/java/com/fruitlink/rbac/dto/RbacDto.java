package com.fruitlink.rbac.dto;

import lombok.Data;
import java.util.List;

public class RbacDto {

    @Data
    public static class RoleResponse {
        private String id;
        private String name;
        private List<PermissionResponse> permissions;
    }

    @Data
    public static class PermissionResponse {
        private String id;
        private String module;
        private String action;
    }

    @Data
    public static class CreatePermissionRequest {
        private String module;
        private String action; // read | write | delete | override
    }

    @Data
    public static class AssignPermissionsRequest {
        private List<String> permissionIds;
    }
}
