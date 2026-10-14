package com.company.corporate_admin_panel.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Email
    private String email;
    private Boolean enabled;
}