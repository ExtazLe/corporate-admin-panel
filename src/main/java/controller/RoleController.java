package com.company.corporate_admin_panel.controller;

import com.company.corporate_admin_panel.dto.RoleCreateRequest;
import com.company.corporate_admin_panel.dto.RoleDto;
import com.company.corporate_admin_panel.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleDto> findAll() {
        return roleService.findAll();
    }

    @PostMapping
    public RoleDto create(@Valid @RequestBody RoleCreateRequest request) {
        return roleService.create(request);
    }

    @PutMapping("/{id}/permissions")
    public RoleDto updatePermissions(@PathVariable Long id,
                                     @RequestBody Set<String> permissions) {
        return roleService.updatePermissions(id, permissions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}