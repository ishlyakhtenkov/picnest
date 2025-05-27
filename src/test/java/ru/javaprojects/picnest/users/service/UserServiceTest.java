package ru.javaprojects.picnest.users.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.context.ActiveProfiles;
import ru.javaprojects.picnest.app.AuthUser;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.javaprojects.picnest.users.UserTestData.*;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
class UserServiceTest {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private UserService userService;

    @Test
    void getOnlineUsersIds() {
        sessionRegistry.registerNewSession("1", new AuthUser(admin));
        sessionRegistry.registerNewSession("2", new AuthUser(user));
        sessionRegistry.registerNewSession("3", new AuthUser(user2));
        sessionRegistry.registerNewSession("4", new AuthUser(user));
        sessionRegistry.registerNewSession("5", new AuthUser(user));

        assertEquals(Set.of(admin.id(), user.id(), user2.id()), userService.getOnlineUsersIds());
    }
}

