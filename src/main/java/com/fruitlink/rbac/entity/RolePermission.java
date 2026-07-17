package com.fruitlink.rbac.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "role_permission")
@Getter @Setter @NoArgsConstructor
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id = new RolePermissionId();

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    public static class RolePermissionId implements Serializable {
        @Column(columnDefinition = "uuid")
        private UUID roleId;
        @Column(columnDefinition = "uuid")
        private UUID permissionId;
    }
}
