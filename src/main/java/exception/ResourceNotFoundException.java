package com.company.corporate_admin_panel.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entity, Object id) {
        super(entity + " не найден: " + id);
    }
}