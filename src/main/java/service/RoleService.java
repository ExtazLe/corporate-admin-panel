package com.company.corporate_admin_panel.service;

import com.company.corporate_admin_panel.dto.RoleCreateRequest;
import com.company.corporate_admin_panel.dto.RoleDto;
import com.company.corporate_admin_panel.entity.Role;
import com.company.corporate_admin_panel.exception.ResourceNotFoundException;
import com.company.corporate_admin_panel.repository.PermissionRepository;
import com.company.corporate_admin_panel.repository.RoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    public List<RoleDto> findAll() {
        return roleRepository.findAll().stream().map(RoleDto::from).toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleDto create(RoleCreateRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Роль уже существует: " + request.getName());
        }
        Role role = new Role();
        role.setName(request.getName());
        if (request.getPermissions() != null) {
            role.setPermissions(permissionRepository.findByNameIn(request.getPermissions()));
        }
        return RoleDto.from(roleRepository.save(role));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleDto updatePermissions(Long roleId, Set<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Роль", roleId));
        role.setPermissions(permissionRepository.findByNameIn(permissionNames));
        return RoleDto.from(roleRepository.save(role));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public void delete(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Роль", roleId);
        }
        roleRepository.deleteById(roleId);
    }
}