package com.company.corporate_admin_panel.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN_PANEL')")
    public Map<String, Object> dashboard() {
        return Map.of(
                "message", "Добро пожаловать в админ-панель",
                "status", "OK"
        );
    }
}