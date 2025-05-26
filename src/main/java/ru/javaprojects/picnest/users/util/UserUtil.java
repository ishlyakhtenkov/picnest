package ru.javaprojects.picnest.users.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import ru.javaprojects.picnest.common.HasEmailAndPassword;
import ru.javaprojects.picnest.common.to.FileTo;
import ru.javaprojects.picnest.common.util.AppUtil;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.to.ProfileTo;
import ru.javaprojects.picnest.users.to.UserTo;

import java.util.List;

import static ru.javaprojects.picnest.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.picnest.common.util.FileUtil.normalizePath;

@UtilityClass
public class UserUtil {

    public static HasEmailAndPassword prepareToSave(HasEmailAndPassword user) {
        String password = user.getPassword();
        user.setPassword(StringUtils.hasText(password) ? PASSWORD_ENCODER.encode(password) : password);
        user.setEmail(user.getEmail().toLowerCase());
        return user;
    }

    public static UserTo asTo(User user) {
        return new UserTo(user.getId(), user.getEmail(), user.getName(), user.getRoles());
    }

    public static ProfileTo asProfileTo(User user) {
        String avatarFileName = user.getAvatar() != null ? user.getAvatar().getFileName() : null;
        String avatarFileLink = user.getAvatar() != null ? user.getAvatar().getFileLink() : null;
        FileTo avatar = (avatarFileName == null || avatarFileLink == null) ? null :
                new FileTo(avatarFileName, avatarFileLink, null, null);
        return new ProfileTo(user.getId(), user.getEmail(), user.getName(), user.getInformation(), avatar);
    }

    public static List<ProfileTo> asProfileTos(List<User> users) {
        return users.stream()
                .map(UserUtil::asProfileTo)
                .toList();
    }

    public static User updateFromTo(User user, UserTo userTo, String avatarFilesPath) {
        String oldEmail = user.getEmail();
        user.setEmail(userTo.getEmail());
        user.setName(userTo.getName());
        user.setRoles(userTo.getRoles());
        if (!user.getEmail().equalsIgnoreCase(oldEmail) && user.getAvatar() != null) {
            user.getAvatar().setFileLink(avatarFilesPath + normalizePath(user.getEmail() + "/" +
                    user.getAvatar().getFileName()));
        }
        return user;
    }

    public static User updateFromTo(User user, ProfileTo profileTo, String avatarFilesPath) {
        user.setName(profileTo.getName());
        user.setInformation(profileTo.getInformation());
        if (profileTo.getAvatar() != null && !profileTo.getAvatar().isEmpty()) {
            user.setAvatar(AppUtil.createFile(profileTo::getAvatar, avatarFilesPath, user.getEmail() + "/"));
        }
        return user;
    }
}
