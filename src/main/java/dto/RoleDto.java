package com.company.corporate_admin_panel.dto;

import com.company.corporate_admin_panel.entity.Permission;
import com.company.corporate_admin_panel.entity.Role;

import java.util.Set;
import java.util.stream.Collectors;

public record RoleDto(Long id, String name, Set<String> permissions) {

    public static RoleDto from(Role role) {
        return new RoleDto(
                role.getId(),
                role.getName(),
                role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet())
        );
    }
}