package com.company.corporate_admin_panel.config;

import com.company.corporate_admin_panel.entity.Permission;
import com.company.corporate_admin_panel.entity.Role;
import com.company.corporate_admin_panel.entity.User;
import com.company.corporate_admin_panel.repository.PermissionRepository;
import com.company.corporate_admin_panel.repository.RoleRepository;
import com.company.corporate_admin_panel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PermissionRepository permissionRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Инициализация данных ===");
        initPermissions();
        initRoles();
        initAdminUser();
        log.info("=== Инициализация завершена ===");
    }

    private void initPermissions() {
        Map<String, String> defaults = Map.of(
                "USER_READ",   "Просмотр пользователей",
                "USER_WRITE",  "Создание и редактирование",
                "USER_DELETE", "Удаление пользователей",
                "ROLE_MANAGE", "Управление ролями",
                "ADMIN_PANEL", "Доступ к админ-панели",
                "AUDIT_VIEW",  "Просмотр аудита"
        );
        defaults.forEach((name, description) -> {
            if (permissionRepository.findByName(name).isEmpty()) {
                Permission p = new Permission();
                p.setName(name);
                p.setDescription(description);
                permissionRepository.save(p);
                log.info("Создано разрешение: {}", name);
            }
        });
    }

    private void initRoles() {
        createRoleIfNotExists("ROLE_ADMIN",
                Set.of("USER_READ", "USER_WRITE", "USER_DELETE",
                        "ROLE_MANAGE", "ADMIN_PANEL", "AUDIT_VIEW"));
        createRoleIfNotExists("ROLE_MANAGER",
                Set.of("USER_READ", "USER_WRITE", "AUDIT_VIEW"));
        createRoleIfNotExists("ROLE_USER",
                Set.of("USER_READ"));
    }

    private void createRoleIfNotExists(String roleName, Set<String> permissionNames) {
        if (roleRepository.findByName(roleName).isPresent()) return;

        Role role = new Role();
        role.setName(roleName);
        role.setPermissions(permissionRepository.findByNameIn(permissionNames));
        roleRepository.save(role);
        log.info("Создана роль: {}", roleName);
    }

    private void initAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@company.local");
        admin.setEnabled(true);
        admin.setRoles(Set.of(roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN не найдена"))));
        userRepository.save(admin);
        log.warn("Создан админ по умолчанию admin/admin123 — СМЕНИТЕ ПАРОЛЬ!");
    }
}