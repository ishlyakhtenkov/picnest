package ru.javaprojects.picnest.users.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.ContentFilesManager;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.mail.MailSender;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.users.error.TokenException;
import ru.javaprojects.picnest.users.error.UserDisabledException;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.model.token.ChangeEmailToken;
import ru.javaprojects.picnest.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.picnest.users.repository.PasswordResetTokenRepository;
import ru.javaprojects.picnest.users.service.UserService;
import ru.javaprojects.picnest.users.to.PasswordResetTo;
import ru.javaprojects.picnest.users.to.ProfileTo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.picnest.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.picnest.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.picnest.common.CommonTestData.ACTION_ATTRIBUTE;
import static ru.javaprojects.picnest.common.CommonTestData.NAME_PARAM;
import static ru.javaprojects.picnest.users.UserTestData.*;
import static ru.javaprojects.picnest.users.service.TokenService.CONFIRMATION_LINK_TEMPLATE;
import static ru.javaprojects.picnest.users.util.UserUtil.asProfileTo;
import static ru.javaprojects.picnest.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.picnest.users.web.ProfileController.PROFILE_URL;
import static ru.javaprojects.picnest.users.web.UniqueEmailValidator.DUPLICATE_ERROR_CODE;

class ProfileControllerTest extends AbstractControllerTest implements ContentFilesManager {
    private static final String PROFILE_RESET_PASSWORD_URL = PROFILE_URL + "/reset-password";
    private static final String PROFILE_CONFIRM_CHANGE_EMAIL_URL = PROFILE_URL + "/change-email/confirm";
    private static final String PROFILE_VIEW_URL = PROFILE_URL + "/%d/view";
    private static final String PROFILE_EDIT_URL = PROFILE_URL + "/edit";
    private static final String RESET_PASSWORD_VIEW = "profile/reset-password";
    private static final String PROFILE_VIEW = "profile/profile";
    private static final String PROFILE_EDIT_VIEW = "profile/profile-edit-form";

    @Value("${content-path.avatars}")
    private String avatarFilesPath;

    @Value("${change-email.confirm-url}")
    private String changeEmailConfirmUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private ChangeEmailTokenRepository changeEmailTokenRepository;

    @MockitoBean
    private MailSender mailSender;

    @Override
    public Path getContentPath() {
        return Paths.get(avatarFilesPath);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(AVATARS_TEST_CONTENT_FILES_PATH);
    }

    @Test
    void showResetPasswordPage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PASSWORD_RESET_TO_ATTRIBUTE))
                .andExpect(view().name(RESET_PASSWORD_VIEW))
                .andExpect(result -> PASSWORD_RESET_TO_MATCHER
                        .assertMatch((PasswordResetTo) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PASSWORD_RESET_TO_ATTRIBUTE),
                                new PasswordResetTo(null, passwordResetToken.getToken())));
    }

    @Test
    void showResetPasswordPageWhenTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, NOT_EXISTING_TOKEN)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("reset-password.token-not-found", null, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void showResetPasswordPageWhenTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, expiredPasswordResetToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("reset-password.token-expired", null, getLocale()), TokenException.class));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showResetPasswordPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void showResetPasswordPageWhenUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, disabledUserPasswordResetToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("user.disabled",
                                new Object[]{disabledUserPasswordResetToken.getUser().getEmail()}, getLocale()),
                        UserDisabledException.class));
    }

    @Test
    void resetPassword() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("reset-password.success",
                        null, getLocale())));
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(passwordResetToken.getUser().id()).getPassword()));
        assertTrue(passwordResetTokenRepository.findByToken(passwordResetToken.getToken()).isEmpty());
    }

    @Test
    void resetPasswordWhenTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, NOT_EXISTING_TOKEN)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("reset-password.token-not-found", null, getLocale()),
                        NotFoundException.class));
    }

    @Test
    void resetPasswordWhenTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, expiredPasswordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("reset-password.token-expired", null, getLocale()), TokenException.class));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD,
                userService.get(expiredPasswordResetToken.getUser().id()).getPassword()));
    }

    @Test
    void resetPasswordWhenUserDisabled() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, disabledUserPasswordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("user.disabled",
                                new Object[]{disabledUserPasswordResetToken.getUser().getEmail()}, getLocale()),
                        UserDisabledException.class));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD,
                userService.get(disabledUserPasswordResetToken.getUser().id()).getPassword()));
    }

    @Test
    void resetPasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .param(PASSWORD_PARAM, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, PASSWORD_PARAM))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD,
                userService.get(passwordResetToken.getUser().id()).getPassword()));
    }

    @Test
    void resetPasswordWithoutTokenParam() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, TOKEN_PARAM))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void resetPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN_PARAM, passwordResetToken.getToken())
                .param(PASSWORD_PARAM, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showProfilePageBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROFILE_VIEW_URL, USER_ID)))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_VIEW))
                .andExpect(model().attribute(USER_ATTRIBUTE, user))
                .andExpect(model().attribute(RATING_ATTRIBUTE, USER_RATING))
                .andExpect(result -> USER_MATCHER.assertMatch((User)Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(USER_ATTRIBUTE), user));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showProfilePageNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROFILE_VIEW_URL, ADMIN_ID)))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_VIEW))
                .andExpect(model().attribute(USER_ATTRIBUTE, admin))
                .andExpect(model().attribute(RATING_ATTRIBUTE, ADMIN_RATING))
                .andExpect(result -> USER_MATCHER.assertMatch((User)Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(USER_ATTRIBUTE), admin));
    }

    @Test
    void showProfilePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(PROFILE_VIEW_URL, USER_ID)))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_VIEW))
                .andExpect(model().attribute(USER_ATTRIBUTE, user))
                .andExpect(model().attribute(RATING_ATTRIBUTE, USER_RATING))
                .andExpect(result -> USER_MATCHER.assertMatch((User)Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(USER_ATTRIBUTE), user));
    }

    @Test
    @WithUserDetails(USER2_MAIL)
    void confirmChangeEmail() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, changeEmailToken.getToken())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(String.format(PROFILE_VIEW_URL, USER2_ID)))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("change-email.email-confirmed",
                        null, getLocale())));
        assertTrue(changeEmailTokenRepository.findByToken(changeEmailToken.getToken()).isEmpty());
        User updated = userService.get(USER2_ID);
        assertEquals(changeEmailToken.getNewEmail(), updated.getEmail());
        assertTrue(Files.exists(Paths.get(updated.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user2.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(avatarFilesPath + FileUtil.normalizePath(user2.getEmail()))));
    }

    @Test
    void confirmChangeEmailUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, changeEmailToken.getToken())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void confirmChangeEmailWhenTokenNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, UUID.randomUUID().toString())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("change-email.token-not-found", null, getLocale()),
                        NotFoundException.class));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void confirmChangeEmailWhenTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, expiredChangeEmailToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("change-email.token-expired", null, getLocale()), TokenException.class));
        assertNotEquals(expiredChangeEmailToken.getNewEmail(), userService.get(ADMIN_ID).getEmail());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void confirmChangeEmailWhenTokenNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_CONFIRM_CHANGE_EMAIL_URL)
                .param(TOKEN_PARAM, changeEmailToken.getToken())
                .with(csrf()))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("change-email.token-not-belongs", null, getLocale()), TokenException.class));
        assertNotEquals(changeEmailToken.getNewEmail(), userService.get(ADMIN_ID).getEmail());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditProfilePage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_EDIT_URL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PROFILE_TO_ATTRIBUTE, asProfileTo(user)))
                .andExpect(view().name(PROFILE_EDIT_VIEW))
                .andExpect(result ->
                        PROFILE_TO_MATCHER.assertMatch((ProfileTo) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(PROFILE_TO_ATTRIBUTE), asProfileTo(user)));
    }

    @Test
    void showEditProfilePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_EDIT_URL)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileWithoutChangingEmail() throws Exception {
        User updatedProfileUser = getUserAfterProfileUpdate(avatarFilesPath);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(getUpdatedProfileToParams(avatarFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(String.format(PROFILE_VIEW_URL, USER_ID)))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated",
                        null, getLocale())));
        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(updatedProfileUser.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileWithChangingEmail() throws Exception {
        User updatedProfileUser = getUserAfterProfileUpdate(avatarFilesPath);
        MultiValueMap<String, String> updatedProfileParams = getUpdatedProfileToParams(avatarFilesPath);
        updatedProfileParams.set(EMAIL_PARAM, NEW_EMAIL);
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(updatedProfileParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(String.format(PROFILE_VIEW_URL, USER_ID)))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated.confirm-email",
                        null, getLocale())));
        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(updatedProfileUser.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user.getAvatar().getFileLink())));

        ChangeEmailToken createdToken = changeEmailTokenRepository.findByUser_Id(USER_ID).orElseThrow();
        assertTrue(createdToken.getExpiryTimestamp().isAfter(LocalDateTime.now()));
        Locale locale = getLocale();
        String changeEmailUrlLinkText = messageSource.getMessage("change-email.message-link-text", null, locale);
        String changeEmailMessageSubject = messageSource.getMessage("change-email.message-subject", null, locale);
        String changeEmailMessageText = messageSource.getMessage("change-email.message-text", null, locale);
        String link = String.format(CONFIRMATION_LINK_TEMPLATE, changeEmailConfirmUrl, createdToken.getToken(),
                changeEmailUrlLinkText);
        String emailText = changeEmailMessageText + link;
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(NEW_EMAIL, changeEmailMessageSubject, emailText);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileWithoutChangingEmailWhenAvatarIsBytesArray() throws Exception {
        User updatedProfileUser = getUserAfterProfileUpdate(avatarFilesPath);
        MultiValueMap<String, String> updatedParams = getUpdatedProfileToParams(avatarFilesPath);
        updatedParams.add(AVATAR_INPUTTED_FILE_BYTES_PARAM,  Arrays.toString(UPDATED_AVATAR_FILE.getBytes()));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(String.format(PROFILE_VIEW_URL, USER_ID)))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated",
                        null, getLocale())));
        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(updatedProfileUser.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileWithoutChangingEmailAndAvatar() throws Exception {
        User updatedProfileUser = geUserAfterProfileUpdateWithOldAvatar();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .params(getUpdatedProfileToParams(avatarFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(String.format(PROFILE_VIEW_URL, USER_ID)))
                .andExpect(flash().attribute(ACTION_ATTRIBUTE, messageSource.getMessage("profile.updated",
                        null, getLocale())));
        USER_MATCHER.assertMatch(userService.get(USER_ID), updatedProfileUser);
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    void updateProfileUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(getUpdatedProfileToParams(avatarFilesPath))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(userService.get(USER_ID).getName(), getUserAfterProfileUpdate(avatarFilesPath).getName());
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUserAfterProfileUpdate(avatarFilesPath).getAvatar().getFileLink())));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileInvalid() throws Exception {
        MultiValueMap<String, String> updatedProfileInvalidParams = getUpdatedProfileToInvalidParams(avatarFilesPath);
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(updatedProfileInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PROFILE_TO_ATTRIBUTE, NAME_PARAM, EMAIL_PARAM, INFORMATION_PARAM))
                .andExpect(view().name(PROFILE_EDIT_VIEW));

        assertArrayEquals(UPDATED_AVATAR_FILE.getBytes(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROFILE_TO_ATTRIBUTE)).getAvatar().getInputtedFileBytes());
        assertEquals(UPDATED_AVATAR_FILE.getOriginalFilename(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROFILE_TO_ATTRIBUTE)).getAvatar().getFileName());
        assertNull(((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROFILE_TO_ATTRIBUTE)).getAvatar().getFileLink());
        assertNotEquals(userService.get(USER_ID).getName(), updatedProfileInvalidParams.get(NAME_PARAM).get(0));
        assertTrue(Files.exists(Paths.get(user.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(getUserAfterProfileUpdate(avatarFilesPath).getAvatar().getFileLink())));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateProfileDuplicateEmail() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedProfileToParams(avatarFilesPath);
        updatedParams.set(EMAIL_PARAM, admin.getEmail());
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, PROFILE_URL)
                .file(UPDATED_AVATAR_FILE)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(PROFILE_TO_ATTRIBUTE, EMAIL_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(PROFILE_EDIT_VIEW));
        assertArrayEquals(UPDATED_AVATAR_FILE.getBytes(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROFILE_TO_ATTRIBUTE)).getAvatar().getInputtedFileBytes());
        assertEquals(UPDATED_AVATAR_FILE.getOriginalFilename(),
                ((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                        .get(PROFILE_TO_ATTRIBUTE)).getAvatar().getFileName());
        assertNull(((ProfileTo) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel()
                .get(PROFILE_TO_ATTRIBUTE)).getAvatar().getFileLink());
        assertNotEquals(userService.get(USER_ID).getEmail(), admin.getEmail());
        assertTrue(Files.exists(Paths.get(admin.getAvatar().getFileLink())));
        assertTrue(Files.notExists(Paths.get(avatarFilesPath + FileUtil.normalizePath(admin.getEmail() + "/" +
                UPDATED_AVATAR_FILE.getOriginalFilename()))));
        assertTrue(changeEmailTokenRepository.findByUser_Id(USER_ID).isEmpty());
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
    }
}
