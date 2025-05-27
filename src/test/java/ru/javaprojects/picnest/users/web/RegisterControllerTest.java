package ru.javaprojects.picnest.users.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.mail.MailSender;
import ru.javaprojects.picnest.users.error.TokenException;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.model.token.RegisterToken;
import ru.javaprojects.picnest.users.repository.RegisterTokenRepository;
import ru.javaprojects.picnest.users.service.UserService;
import ru.javaprojects.picnest.users.to.RegisterTo;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.picnest.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.picnest.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.picnest.common.CommonTestData.ACTION_ATTRIBUTE;
import static ru.javaprojects.picnest.common.CommonTestData.NAME_PARAM;
import static ru.javaprojects.picnest.users.UserTestData.*;
import static ru.javaprojects.picnest.users.service.TokenService.CONFIRMATION_LINK_TEMPLATE;
import static ru.javaprojects.picnest.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.picnest.users.web.RegisterController.REGISTER_URL;
import static ru.javaprojects.picnest.users.web.UniqueEmailValidator.DUPLICATE_ERROR_CODE;

class RegisterControllerTest extends AbstractControllerTest {
    private static final String CONFIRM_REGISTER_URL = REGISTER_URL + "/confirm";
    private static final String REGISTER_PAGE_VIEW = "profile/register";

    @MockitoBean
    private MailSender mailSender;

    @Autowired
    private RegisterTokenRepository tokenRepository;

    @Autowired
    private UserService userService;

    @Value("${register.confirm-url}")
    private String confirmRegisterUrl;

    @Test
    void showRegisterPage() throws Exception {
        perform(MockMvcRequestBuilders.get(REGISTER_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(REGISTER_TO_ATTRIBUTE))
                .andExpect(view().name(REGISTER_PAGE_VIEW))
                .andExpect(result ->
                        REGISTER_TO_MATCHER.assertMatch((RegisterTo) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(REGISTER_TO_ATTRIBUTE), new RegisterTo()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showRegisterPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REGISTER_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void register() throws Exception {
        RegisterTo newRegisterTo = getRegisterTo();
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(getRegisterToParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("register.check-your-email",
                        null, getLocale())));
        RegisterToken createdToken = tokenRepository.findByEmailIgnoreCase(newRegisterTo.getEmail()).orElseThrow();
        assertTrue(createdToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        assertEquals(newRegisterTo.getName(), createdToken.getName());
        assertTrue(PASSWORD_ENCODER.matches(newRegisterTo.getPassword(), createdToken.getPassword()));
        Locale locale = getLocale();
        String confirmRegisterUrlLinkText = messageSource.getMessage("register.message-link-text", null, locale);
        String confirmRegisterMessageSubject = messageSource.getMessage("register.message-subject", null, locale);
        String confirmRegisterMessageText = messageSource.getMessage("register.message-text", null, locale);
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, confirmRegisterUrl, createdToken.getToken(),
                confirmRegisterUrlLinkText);
        String emailText = confirmRegisterMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(newRegisterTo.getEmail(), confirmRegisterMessageSubject,
                emailText);
    }

    @Test
    void registerWhenRegisterTokenForThatEmailAlreadyExists() throws Exception {
        RegisterTo newRegisterTo = getRegisterTo();
        newRegisterTo.setEmail(registerToken.getEmail());
        MultiValueMap<String, String> registerToParams = getRegisterToParams();
        registerToParams.set(EMAIL_PARAM, registerToken.getEmail());
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(registerToParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("register.check-your-email",
                        null, getLocale())));
        RegisterToken updatedToken = tokenRepository.findByEmailIgnoreCase(newRegisterTo.getEmail()).orElseThrow();
        assertTrue(updatedToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        assertEquals(newRegisterTo.getName(), updatedToken.getName());
        assertTrue(PASSWORD_ENCODER.matches(newRegisterTo.getPassword(), updatedToken.getPassword()));
        Locale locale = getLocale();
        String confirmRegisterUrlLinkText = messageSource.getMessage("register.message-link-text", null, locale);
        String confirmRegisterMessageSubject = messageSource.getMessage("register.message-subject", null, locale);
        String confirmRegisterMessageText = messageSource.getMessage("register.message-text", null, locale);
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, confirmRegisterUrl, updatedToken.getToken(),
                confirmRegisterUrlLinkText);
        String emailText = confirmRegisterMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(newRegisterTo.getEmail(), confirmRegisterMessageSubject,
                emailText);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void registerAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(getRegisterToParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerInvalid() throws Exception {
        MultiValueMap<String, String> newToInvalidParams = getRegisterToInvalidParams();
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(newToInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(REGISTER_TO_ATTRIBUTE, EMAIL_PARAM, NAME_PARAM,
                        PASSWORD_PARAM))
                .andExpect(view().name(REGISTER_PAGE_VIEW));
        assertTrue(tokenRepository.findByEmailIgnoreCase(newToInvalidParams.get(EMAIL_PARAM).get(0)).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    void registerDuplicateEmail() throws Exception {
        MultiValueMap<String, String> newToParams = getRegisterToParams();
        newToParams.set(EMAIL_PARAM, USER_MAIL);
        perform(MockMvcRequestBuilders.post(REGISTER_URL)
                .params(newToParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(REGISTER_TO_ATTRIBUTE, EMAIL_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(REGISTER_PAGE_VIEW));
        assertTrue(tokenRepository.findByEmailIgnoreCase(newToParams.get(EMAIL_PARAM).get(0)).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    void confirmRegister() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_REGISTER_URL)
                .param(TOKEN_PARAM, registerToken.getToken())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("register.confirmed", null,
                        getLocale())));
        assertTrue(tokenRepository.findByToken(registerToken.getToken()).isEmpty());
        User created = userService.getByEmail(registerToken.getEmail());
        User newUser = getNewUser();
        newUser.setId(created.id());
        USER_MATCHER.assertMatch(created, newUser);
        assertTrue(PASSWORD_ENCODER.matches(newUser.getPassword(), created.getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void confirmRegisterAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(CONFIRM_REGISTER_URL)
                .param(TOKEN_PARAM, registerToken.getToken())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmRegisterWhenTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_REGISTER_URL)
                .param(TOKEN_PARAM, UUID.randomUUID().toString())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("register.token-not-found", null, getLocale()), NotFoundException.class));
    }

    @Test
    void confirmRegisterWhenTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(CONFIRM_REGISTER_URL)
                .param(TOKEN_PARAM, expiredRegisterToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("register.token-expired", null, getLocale()), TokenException.class));
        assertThrows(NotFoundException.class, () -> userService.getByEmail(expiredRegisterToken.getEmail()));
    }
}
