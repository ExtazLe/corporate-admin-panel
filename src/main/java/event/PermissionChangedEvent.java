package com.company.corporate_admin_panel.event;

import java.util.Set;

public record PermissionChangedEvent(Long userId, Set<String> newPermissions) {
}