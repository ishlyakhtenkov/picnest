package ru.javaprojects.picnest.users.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import ru.javaprojects.picnest.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.picnest.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.picnest.users.repository.RegisterTokenRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.javaprojects.picnest.users.UserTestData.*;

@SpringBootTest
@Sql(scripts = "classpath:data.sql", config = @SqlConfig(encoding = "UTF-8"))
@ActiveProfiles({"dev", "test"})
class TokenCleanerTest {

    @Autowired
    private TokenCleaner tokenCleaner;

    @Autowired
    private RegisterTokenRepository registerTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private ChangeEmailTokenRepository changeEmailTokenRepository;

    @Test
    void deleteExpiredTokens() {
        tokenCleaner.deleteExpiredTokens();
        assertEquals(List.of(registerToken), registerTokenRepository.findAll());
        assertEquals(List.of(passwordResetToken, disabledUserPasswordResetToken),
                passwordResetTokenRepository.findAll(Sort.by("expiryTimestamp")));
        assertEquals(List.of(changeEmailToken), changeEmailTokenRepository.findAll());
    }
}
