package ru.javaprojects.picnest.users.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.ContentFilesManager;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.users.service.UserService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.picnest.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.picnest.common.CommonTestData.HOME_URL;
import static ru.javaprojects.picnest.common.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.picnest.users.UserTestData.*;
import static ru.javaprojects.picnest.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.picnest.users.web.UserManagementController.USERS_URL;

class UserManagementRestControllerTest extends AbstractControllerTest implements ContentFilesManager {
    private static final String USERS_URL_SLASH = USERS_URL + "/";
    private static final String USERS_CHANGE_PASSWORD_URL = USERS_URL + "/change-password/";

    @Value("${content-path.avatars}")
    private String avatarFilesPath;

    @Autowired
    private UserService service;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Override
    public Path getContentPath() {
        return Paths.get(avatarFilesPath);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(AVATARS_TEST_CONTENT_FILES_PATH);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enable() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(service.get(USER_ID).isEnabled());

        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED_PARAM, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enableYourself() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + ADMIN_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("user.forbidden-disable-yourself", null, getLocale())))
                .andExpect(problemInstance(USERS_URL_SLASH + ADMIN_ID));
        assertTrue(service.get(ADMIN_ID).isEnabled());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enableNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + NOT_EXISTING_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(USERS_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void enableUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void enableForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    void instantBan() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(HOME_URL).with(user(new AuthUser(user))))
                .andExpect(status().isOk());
        HttpSession userSession = actions.andReturn().getRequest().getSession();
        sessionRegistry.registerNewSession(Objects.requireNonNull(userSession).getId(), new AuthUser(user));
        assertFalse(sessionRegistry.getSessionInformation(userSession.getId()).isExpired());
        ResultActions actions2 = perform(MockMvcRequestBuilders.get(HOME_URL).with(user(new AuthUser(user))))
                .andExpect(status().isOk());
        HttpSession userSession2 = actions2.andReturn().getRequest().getSession();
        sessionRegistry.registerNewSession(Objects.requireNonNull(userSession2).getId(), new AuthUser(user));
        assertFalse(sessionRegistry.getSessionInformation(userSession2.getId()).isExpired());

        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .with(user(new AuthUser(admin)))
                .param(ENABLED_PARAM, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(sessionRegistry.getSessionInformation(userSession.getId()).isExpired());
        assertTrue(sessionRegistry.getSessionInformation(userSession2.getId()).isExpired());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void delete() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(HOME_URL).with(user(new AuthUser(user))))
                .andExpect(status().isOk());
        HttpSession userSession = actions.andReturn().getRequest().getSession();
        sessionRegistry.registerNewSession(Objects.requireNonNull(userSession).getId(), new AuthUser(user));
        assertFalse(sessionRegistry.getSessionInformation(userSession.getId()).isExpired());
        ResultActions actions2 = perform(MockMvcRequestBuilders.get(HOME_URL).with(user(new AuthUser(user))))
                .andExpect(status().isOk());
        HttpSession userSession2 = actions2.andReturn().getRequest().getSession();
        sessionRegistry.registerNewSession(Objects.requireNonNull(userSession2).getId(), new AuthUser(user));
        assertFalse(sessionRegistry.getSessionInformation(userSession2.getId()).isExpired());

        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + USER_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> service.get(USER_ID));
        assertTrue(Files.notExists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(sessionRegistry.getSessionInformation(userSession.getId()).isExpired());
        assertTrue(sessionRegistry.getSessionInformation(userSession2.getId()).isExpired());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteYourself() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + ADMIN_ID)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("user.forbidden-delete-yourself", null, getLocale())))
                .andExpect(problemInstance(USERS_URL_SLASH + ADMIN_ID));
        assertDoesNotThrow(() -> service.get(ADMIN_ID));
        assertTrue(Files.exists(Paths.get(admin.getAvatar().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + NOT_EXISTING_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(USERS_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    void deleteUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + USER_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> service.get(USER_ID));
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + USER_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.get(USER_ID));
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, service.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePasswordNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + NOT_EXISTING_ID)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID},
                        getLocale())))
                .andExpect(problemInstance(USERS_CHANGE_PASSWORD_URL + NOT_EXISTING_ID));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD_PARAM, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("changePassword.password: Password size must be between 5 and 32"))
                .andExpect(jsonPath("$.invalid_params").value("Password size must be between 5 and 32"))
                .andExpect(problemInstance(USERS_CHANGE_PASSWORD_URL + USER_ID));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, service.get(USER_ID).getPassword()));
    }

    @Test
    void changePasswordUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, service.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePasswordForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, service.get(USER_ID).getPassword()));
    }
}