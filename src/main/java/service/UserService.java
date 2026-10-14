package com.company.corporate_admin_panel.service;

import com.company.corporate_admin_panel.dto.UserDto;
import com.company.corporate_admin_panel.dto.UserUpdateRequest;
import com.company.corporate_admin_panel.entity.Permission;
import com.company.corporate_admin_panel.entity.User;
import com.company.corporate_admin_panel.event.PermissionChangedEvent;
import com.company.corporate_admin_panel.exception.ResourceNotFoundException;
import com.company.corporate_admin_panel.repository.RoleRepository;
import com.company.corporate_admin_panel.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", id));
        return UserDto.from(user);
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public UserDto update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", id));
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь", id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public UserDto assignRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", userId));
        user.setRoles(new HashSet<>(roleRepository.findAllById(roleIds)));
        User saved = userRepository.save(user);

        Set<String> permissions = saved.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        eventPublisher.publishEvent(new PermissionChangedEvent(userId, permissions));
        return UserDto.from(saved);
    }
}