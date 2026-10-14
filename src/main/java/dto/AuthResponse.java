package com.company.corporate_admin_panel.dto;

import java.util.Set;

public record AuthResponse(String token, String username, Set<String> roles) {
}