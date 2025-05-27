package ru.javaprojects.picnest.users.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.mail.MailSender;
import ru.javaprojects.picnest.users.model.token.ChangeEmailToken;
import ru.javaprojects.picnest.users.repository.ChangeEmailTokenRepository;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.javaprojects.picnest.users.UserTestData.*;
import static ru.javaprojects.picnest.users.service.TokenService.CONFIRMATION_LINK_TEMPLATE;

@SpringBootTest
@Sql(scripts = "classpath:data.sql", config = @SqlConfig(encoding = "UTF-8"))
@ActiveProfiles({"dev", "test"})
class ChangeEmailServiceTest {

    @Value("${change-email.confirm-url}")
    private String confirmChangeEmailUrl;

    @MockitoBean
    private MailSender mailSender;

    @Autowired
    private ChangeEmailService changeEmailService;

    @Autowired
    private ChangeEmailTokenRepository changeEmailTokenRepository;

    @Autowired
    private MessageSource messageSource;

    @Test
    void changeEmail() {
        changeEmailService.changeEmail(USER_ID, NEW_EMAIL);
        ChangeEmailToken createdToken = changeEmailTokenRepository.findByUser_Id(USER_ID).orElseThrow();
        assertTrue(createdToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, confirmChangeEmailUrl, createdToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL, changeEmailMessageSubject, emailText);
    }

    @Test
    void changeEmailWhenSomeoneHasThisEmail() {
        IllegalRequestDataException exception =
                assertThrows(IllegalRequestDataException.class, () -> changeEmailService.changeEmail(USER_ID, ADMIN_MAIL));
        assertEquals("change-email.already-in-use", exception.getMessageCode());
        assertEquals("email=" + ADMIN_MAIL + " already in use", exception.getMessage());
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void changeEmailWhenAlreadyHasThisEmail() {
        IllegalRequestDataException exception =
                assertThrows(IllegalRequestDataException.class, () -> changeEmailService.changeEmail(USER_ID, USER_MAIL));
        assertEquals("change-email.already-has-email", exception.getMessageCode());
        assertEquals("user with id=" + USER_ID + " already has email=" + USER_MAIL, exception.getMessage());
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void changeEmailWhenChangeEmailTokenExists() {
        changeEmailService.changeEmail(ADMIN_ID, NEW_EMAIL);
        ChangeEmailToken updatedToken = changeEmailTokenRepository.findByUser_Id(ADMIN_ID).orElseThrow();
        assertTrue(updatedToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        assertEquals(NEW_EMAIL, updatedToken.getNewEmail());
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, confirmChangeEmailUrl, updatedToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL, changeEmailMessageSubject, emailText);
    }

    @Test
    void changeEmailWhenSomeoneHasTokenWithThisEmail() {
        changeEmailService.changeEmail(USER_ID, NEW_EMAIL_SOMEONE_HAS_TOKEN);
        ChangeEmailToken createdToken = changeEmailTokenRepository.findByUser_Id(USER_ID).orElseThrow();
        assertTrue(createdToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        Locale locale = LocaleContextHolder.getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, confirmChangeEmailUrl, createdToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL_SOMEONE_HAS_TOKEN, changeEmailMessageSubject,
                emailText);
    }
}
