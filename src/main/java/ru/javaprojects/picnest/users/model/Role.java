package ru.javaprojects.picnest.users.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;

public enum Role implements GrantedAuthority {
    USER,
    ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
