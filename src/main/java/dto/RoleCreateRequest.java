package com.company.corporate_admin_panel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RoleCreateRequest {
    @NotBlank
    private String name;
    private Set<String> permissions;
}