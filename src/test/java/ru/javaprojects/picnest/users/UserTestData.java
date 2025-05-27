package ru.javaprojects.picnest.users;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.picnest.MatcherFactory;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.users.model.Role;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.model.token.ChangeEmailToken;
import ru.javaprojects.picnest.users.model.token.PasswordResetToken;
import ru.javaprojects.picnest.users.model.token.RegisterToken;
import ru.javaprojects.picnest.users.to.PasswordResetTo;
import ru.javaprojects.picnest.users.to.ProfileTo;
import ru.javaprojects.picnest.users.to.RegisterTo;
import ru.javaprojects.picnest.users.to.UserTo;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.javaprojects.picnest.common.CommonTestData.*;
import static ru.javaprojects.picnest.users.model.Role.USER;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(User.class, "password", "registered");

    public static final MatcherFactory.Matcher<UserTo> USER_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(UserTo.class);

    public static final MatcherFactory.Matcher<ProfileTo> PROFILE_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(ProfileTo.class);

    public static final MatcherFactory.Matcher<RegisterTo> REGISTER_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(RegisterTo.class);

    public static final MatcherFactory.Matcher<PasswordResetTo> PASSWORD_RESET_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(PasswordResetTo.class);

    public static final String AVATARS_TEST_CONTENT_FILES_PATH = "src/test/test-content-files/avatars";

    public static final String USER_ATTRIBUTE = "user";
    public static final String USERS_ATTRIBUTE = "users";
    public static final String USER_TO_ATTRIBUTE = "userTo";
    public static final String PROFILE_TO_ATTRIBUTE = "profileTo";
    public static final String ROLES_ATTRIBUTE = "roles";
    public static final String REGISTER_TO_ATTRIBUTE = "registerTo";
    public static final String PASSWORD_RESET_TO_ATTRIBUTE = "passwordResetTo";
    public static final String ONLINE_USERS_IDS_ATTRIBUTE = "onlineUsersIds";
    public static final String PROFILES_PAGE_ATTRIBUTE = "profilesPage";
    public static final String RATING_ATTRIBUTE = "rating";
    public static final String ERROR_ATTRIBUTE = "error";

    public static final String USERNAME_PARAM = "username";
    public static final String PASSWORD_PARAM = "password";
    public static final String ENABLED_PARAM = "enabled";
    public static final String EMAIL_PARAM = "email";
    public static final String INFORMATION_PARAM = "information";
    public static final String TOKEN_PARAM = "token";
    public static final String KEYWORD_PARAM = "keyword";
    public static final String ROLES_PARAM = "roles";
    public static final String AVATAR_FILE_NAME_PARAM = "avatar.fileName";
    public static final String AVATAR_FILE_LINK_PARAM = "avatar.fileLink";
    public static final String AVATAR_INPUTTED_FILE_BYTES_PARAM = "avatar.inputtedFileBytes";

    public static final String NEW_PASSWORD = "newPassword";
    public static final String NEW_EMAIL = "newEmail@gmail.com";
    public static final String NEW_EMAIL_SOMEONE_HAS_TOKEN = "someNew@gmail.com";
    public static final String INVALID_PASSWORD = "pass";
    public static final String INVALID_EMAIL = "invEmail.gmail.ru";
    public static final String NOT_EXISTING_EMAIL = "notExisting@gmail.com";
    public static final String NOT_EXISTING_TOKEN = UUID.randomUUID().toString();

    public static final String USER_MAIL = "user@gmail.com";
    public static final String ADMIN_MAIL = "admin@gmail.com";
    public static final String USER2_MAIL = "user2@gmail.com";
    public static final String DISABLED_USER_MAIL = "userDisabled@gmail.com";

    public static final long USER_ID = 100000;
    public static final long ADMIN_ID = 100001;
    public static final long USER2_ID = 100002;
    public static final long DISABLED_USER_ID = 100003;

    public static final int USER_RATING = -1;
    public static final int ADMIN_RATING = -1;

    public static final User user = new User(USER_ID, USER_MAIL, "John Doe", "Some info", "password", true,
            Set.of(USER), new File("cool_user.jpg", "./content/avatars/user@gmail.com/cool_user.jpg"));

    public static final User admin = new User(ADMIN_ID, ADMIN_MAIL, "Jack",
            "Java developer with 10 years of production experience.", "admin", true, Set.of(USER, Role.ADMIN),
            new File("admin.jpg", "./content/avatars/admin@gmail.com/admin.jpg"));

    public static final User user2 = new User(USER2_ID, USER2_MAIL, "Alice Key", null, "somePassword", true,
            Set.of(USER), new File("cat.jpg", "./content/avatars/user2@gmail.com/cat.jpg"));

    public static final User disabledUser = new User(DISABLED_USER_ID, DISABLED_USER_MAIL, "Freeman25", "password",
            false, Set.of(USER));

    public static final MockMultipartFile UPDATED_AVATAR_FILE = new MockMultipartFile("avatar.inputtedFile",
            "updated_photo.png", MediaType.IMAGE_PNG_VALUE, "updated avatar file content bytes".getBytes());

    public static User getNewUser() {
        return new User(null, "new@gmail.com", "newName", "newPassword", true, Set.of(USER));
    }

    public static User getUpdatedUser(String avatarFilesPath) {
        String updatedEmail = "updated@gmail.com";
        return new User(USER_ID, updatedEmail, "updatedName", user.getInformation(), user.getPassword(), user.isEnabled(),
                Set.of(USER, Role.ADMIN),
                new File(user.getAvatar().getFileName(), avatarFilesPath + updatedEmail + "/" +
                        user.getAvatar().getFileName()));
    }

    public static User getUpdatedUserWithOldEMail() {
        return new User(USER_ID, USER_MAIL, "updatedName", user.getInformation(), user.getPassword(), user.isEnabled(),
                Set.of(USER, Role.ADMIN), new File(user.getAvatar().getFileName(), user.getAvatar().getFileLink()));
    }

    public static MultiValueMap<String, String> getNewUserParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        User newUser = getNewUser();
        params.add(NAME_PARAM, newUser.getName());
        params.add(EMAIL_PARAM, newUser.getEmail());
        params.add(ROLES_PARAM, newUser.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")));
        params.add(PASSWORD_PARAM, newUser.getPassword());
        params.add(ENABLED_PARAM, String.valueOf(newUser.isEnabled()));
        return params;
    }

    public static MultiValueMap<String, String> getNewUserInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, HTML_TEXT);
        params.add(EMAIL_PARAM, INVALID_EMAIL);
        params.add(ROLES_PARAM, EMPTY_STRING);
        params.add(PASSWORD_PARAM, INVALID_PASSWORD);
        params.add(ENABLED_PARAM, String.valueOf(true));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedUserParams(String avatarFilesPath) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        User updatedUser = getUpdatedUser(avatarFilesPath);
        params.add(ID_PARAM, String.valueOf(USER_ID));
        params.add(NAME_PARAM, updatedUser.getName());
        params.add(EMAIL_PARAM, updatedUser.getEmail());
        params.add(ROLES_PARAM, updatedUser.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedUserInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(ID_PARAM, String.valueOf(USER_ID));
        params.add(NAME_PARAM, HTML_TEXT);
        params.add(EMAIL_PARAM, INVALID_EMAIL);
        params.add(ROLES_PARAM, EMPTY_STRING);
        return params;
    }

    public static RegisterTo getRegisterTo() {
        return new RegisterTo(null, "newuser@gmail.com", "newName", "newPassword");
    }

    public static MultiValueMap<String, String> getRegisterToParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        RegisterTo newRegisterTo = getRegisterTo();
        params.add(EMAIL_PARAM, newRegisterTo.getEmail());
        params.add(NAME_PARAM, newRegisterTo.getName());
        params.add(PASSWORD_PARAM, newRegisterTo.getPassword());
        return params;
    }

    public static MultiValueMap<String, String> getRegisterToInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(EMAIL_PARAM, INVALID_EMAIL);
        params.add(NAME_PARAM, HTML_TEXT);
        params.add(PASSWORD_PARAM, EMPTY_STRING);
        return params;
    }

    public static final RegisterToken expiredRegisterToken = new RegisterToken(100004L,
            "5a99dd09-d23f-44bb-8d41-b6ff44275d01", parseLocalDateTime("2024-08-06 19:35:56"), "some@gmail.com",
            "someName", "{noop}somePassword");

    public static final RegisterToken registerToken = new RegisterToken(100005L, "52bde839-9779-4005-b81c-9131c9590d79",
            parseLocalDateTime("2052-05-24 16:42:03"), "new@gmail.com", "newName", "{noop}newPassword");

    public static final PasswordResetToken passwordResetToken = new PasswordResetToken(100006L,
            "5a99dd09-d23f-44bb-8d41-b6ff44275x97", parseLocalDateTime("2052-02-05 12:10:00"), user);

    public static final PasswordResetToken expiredPasswordResetToken = new PasswordResetToken(100007L,
            "52bde839-9779-4005-b81c-9131c9590b41", parseLocalDateTime("2022-02-06 19:35:56"), user2);

    public static final PasswordResetToken disabledUserPasswordResetToken = new PasswordResetToken(100008L,
            "54ghh534-9778-4005-b81c-9131c9590c63", parseLocalDateTime("2052-04-25 13:48:14"), disabledUser);

    public static final ChangeEmailToken changeEmailToken = new ChangeEmailToken(100010L,
            "1a43dx02-x23x-42xx-8r42-x6ff44275y67", parseLocalDateTime("2052-01-22 06:17:32"), "someNew@gmail.com", user2);

    public static final ChangeEmailToken expiredChangeEmailToken = new ChangeEmailToken(100009L,
            "5a49dd09-g23f-44bb-8d41-b6ff44275s56", parseLocalDateTime("2024-08-05 21:49:01"), "some@gmail.com", admin);

    public static User getUserAfterProfileUpdate(String avatarFilesPath) {
        return new User(USER_ID, user.getEmail(), "updatedName", "updated info", user.getPassword(), user.isEnabled(),
                user.getRoles(),  new File(UPDATED_AVATAR_FILE.getOriginalFilename(), avatarFilesPath +
                FileUtil.normalizePath(user.getEmail() + "/" + UPDATED_AVATAR_FILE.getOriginalFilename())));
    }

    public static User geUserAfterProfileUpdateWithOldAvatar() {
        return new User(USER_ID, user.getEmail(), "updatedName", "updated info", user.getPassword(), user.isEnabled(),
                user.getRoles(), user.getAvatar());
    }

    public static MultiValueMap<String, String> getUpdatedProfileToParams(String avatarFilesPath) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        User updatedProfileUser = getUserAfterProfileUpdate(avatarFilesPath);
        params.add(ID_PARAM, String.valueOf(USER_ID));
        params.add(NAME_PARAM, updatedProfileUser.getName());
        params.add(EMAIL_PARAM, updatedProfileUser.getEmail());
        params.add(INFORMATION_PARAM, updatedProfileUser.getInformation());
        params.add(AVATAR_FILE_NAME_PARAM, updatedProfileUser.getAvatar().getFileName());
        params.add(AVATAR_FILE_LINK_PARAM, updatedProfileUser.getAvatar().getFileLink());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedProfileToInvalidParams(String avatarFilesPath) {
        MultiValueMap<String, String> params = getUpdatedProfileToParams(avatarFilesPath);
        params.set(NAME_PARAM, LONG_STRING);
        params.set(EMAIL_PARAM, INVALID_EMAIL);
        params.set(INFORMATION_PARAM, HTML_TEXT);
        return params;
    }
}
