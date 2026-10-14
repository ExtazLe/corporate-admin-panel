package com.company.corporate_admin_panel.dto;

import com.company.corporate_admin_panel.entity.Role;
import com.company.corporate_admin_panel.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserDto(
        Long id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}