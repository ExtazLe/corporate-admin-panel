package com.company.corporate_admin_panel.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLogListener {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @EventListener
    public void onPermissionChanged(PermissionChangedEvent event) {
        audit.info("Изменены права пользователя {} → {}",
                event.userId(), event.newPermissions());
    }
}