
package com.company.corporate_admin_panel.service;

import com.company.corporate_admin_panel.dto.AuthResponse;
import com.company.corporate_admin_panel.dto.LoginRequest;
import com.company.corporate_admin_panel.dto.RegisterRequest;
import com.company.corporate_admin_panel.entity.Role;
import com.company.corporate_admin_panel.entity.User;
import com.company.corporate_admin_panel.repository.RoleRepository;
import com.company.corporate_admin_panel.repository.UserRepository;
import com.company.corporate_admin_panel.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authManager,
                       UserDetailsService userDetailsService,
                       JwtService jwtService,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails details = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(details);

        Set<String> roles = details.getAuthorities().stream()
                .map(Object::toString)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        return new AuthResponse(token, details.getUsername(), roles);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь уже существует");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(true);

        roleRepository.findByName("ROLE_USER").ifPresent(r -> user.setRoles(Set.of(r)));
        userRepository.save(user);

        UserDetails details = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(details);
        return new AuthResponse(token, user.getUsername(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }
}