package ru.javaprojects.picnest.users.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.mail.MailSender;
import ru.javaprojects.picnest.common.util.JsonUtil;
import ru.javaprojects.picnest.users.error.UserDisabledException;
import ru.javaprojects.picnest.users.model.token.PasswordResetToken;
import ru.javaprojects.picnest.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.picnest.users.service.UserService;
import ru.javaprojects.picnest.users.to.ProfileTo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.picnest.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.picnest.common.CommonTestData.getPageableParams;
import static ru.javaprojects.picnest.users.UserTestData.*;
import static ru.javaprojects.picnest.users.service.TokenService.CONFIRMATION_LINK_TEMPLATE;
import static ru.javaprojects.picnest.users.util.UserUtil.asProfileTo;
import static ru.javaprojects.picnest.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.picnest.users.web.ProfileController.PROFILE_URL;

class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String PROFILE_URL_SLASH = PROFILE_URL + "/";
    private static final String PROFILE_FORGOT_PASSWORD_URL = PROFILE_URL + "/forgot-password";
    private static final String PROFILE_CHANGE_PASSWORD_URL = PROFILE_URL + "/change-password";

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserService userService;

    @MockitoBean
    private MailSender mailSender;

    @Value("${password-reset.confirm-url}")
    private String passwordResetUrl;

    @Test
    void forgotPassword() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, USER_MAIL)
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken createdToken = passwordResetTokenRepository.findByUserEmailIgnoreCase(USER_MAIL).orElseThrow();
        assertTrue(createdToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        String passwordResetUrlLinkText = messageSource.getMessage("reset-password.message-link-text", null, getLocale());
        String passwordResetMessageSubject = messageSource.getMessage("reset-password.message-subject", null, getLocale());
        String passwordResetMessageText = messageSource.getMessage("reset-password.message-text", null, getLocale());
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, passwordResetUrl, createdToken.getToken(),
                passwordResetUrlLinkText);
        String emailText = passwordResetMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(USER_MAIL, passwordResetMessageSubject, emailText);
    }

    @Test
    void forgotPasswordWhenPasswordResetTokenExists() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, passwordResetToken.getUser().getEmail())
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken updatedToken = passwordResetTokenRepository
                .findByUserEmailIgnoreCase(passwordResetToken.getUser().getEmail()).orElseThrow();
        assertTrue(updatedToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        String passwordResetUrlLinkText = messageSource.getMessage("reset-password.message-link-text", null, getLocale());
        String passwordResetMessageSubject = messageSource.getMessage("reset-password.message-subject", null, getLocale());
        String passwordResetMessageText = messageSource.getMessage("reset-password.message-text", null, getLocale());
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, passwordResetUrl, updatedToken.getToken(),
                passwordResetUrlLinkText);
        String emailText = passwordResetMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(passwordResetToken.getUser().getEmail(),
                passwordResetMessageSubject, emailText);
    }

    @Test
    void forgotPasswordWhenEmailNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, NOT_EXISTING_EMAIL)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.user", new Object[]{NOT_EXISTING_EMAIL},
                        getLocale())))
                .andExpect(problemInstance(PROFILE_FORGOT_PASSWORD_URL));
        assertTrue(passwordResetTokenRepository.findByUserEmailIgnoreCase(NOT_EXISTING_EMAIL).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void forgotPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, ADMIN_MAIL)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(passwordResetTokenRepository.findByUserEmailIgnoreCase(ADMIN_MAIL).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    void forgotPasswordWhenUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL_PARAM, DISABLED_USER_MAIL)
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        UserDisabledException.class))
                .andExpect(problemTitle(HttpStatus.FORBIDDEN.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.FORBIDDEN.value()))
                .andExpect(problemDetail(messageSource.getMessage("user.disabled", new Object[]{DISABLED_USER_MAIL},
                        getLocale())))
                .andExpect(problemInstance(PROFILE_FORGOT_PASSWORD_URL));
        assertEquals(disabledUserPasswordResetToken.getExpiryTimestamp(),
                passwordResetTokenRepository.findByUserEmailIgnoreCase(DISABLED_USER_MAIL)
                        .orElseThrow().getExpiryTimestamp());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(USER_ID).getPassword()));
    }

    @Test
    void changePasswordUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD_PARAM, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("changePassword.password: Password size must be between 5 and 32"))
                .andExpect(jsonPath("$.invalid_params").value("Password size must be between 5 and 32"))
                .andExpect(problemInstance(PROFILE_CHANGE_PASSWORD_URL));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, userService.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getProfilesByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROFILE_URL_SLASH + "by-keyword")
                .param(KEYWORD_PARAM, admin.getName())
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProfileTo> profiles =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProfileTo.class);
        ProfileTo adminTGo = asProfileTo(admin);
        PROFILE_TO_MATCHER.assertMatch(profiles, List.of(asProfileTo(admin)));
    }

    @Test
    void getProfilesByKeywordUnauthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(PROFILE_URL_SLASH + "by-keyword")
                .param(KEYWORD_PARAM, admin.getName())
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<ProfileTo> profiles =
                JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), ProfileTo.class);
        PROFILE_TO_MATCHER.assertMatch(profiles, List.of(asProfileTo(admin)));
    }
}

