package com.company.admin.service;

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
        return toDto(user);
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public UserDto update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", id));
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        return toDto(userRepository.save(user));
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

        Set<String> perms = saved.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        eventPublisher.publishEvent(new PermissionChangedEvent(userId, perms));

        return toDto(saved);
    }

    // Mapper — приватный метод прямо тут
    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}