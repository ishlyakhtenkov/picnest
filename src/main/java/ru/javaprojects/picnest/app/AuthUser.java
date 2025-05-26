package ru.javaprojects.picnest.app;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.javaprojects.picnest.users.model.User;

import static java.util.Objects.requireNonNull;

public class AuthUser extends org.springframework.security.core.userdetails.User {

    @Getter
    @NonNull
    private final User user;

    public AuthUser(@NonNull User user) {
        super(user.getEmail(), user.getPassword(), user.isEnabled(), true, true, true, user.getRoles());
        this.user = user;
    }

    public static AuthUser safeGet() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        return (principal instanceof AuthUser au) ? au : null;
    }

    public static AuthUser get() {
        return requireNonNull(safeGet(), "No authorized user found");
    }

    public static User authUser() {
        return get().getUser();
    }

    public static long authId() {
        return get().id();
    }

    public static boolean isAdmin() {
        return authUser().isAdmin();
    }

    public long id() {
        return user.id();
    }

    @Override
    public String toString() {
        return "AuthUser:" + user.getId() + '[' + user.getEmail() + ']';
    }
}
