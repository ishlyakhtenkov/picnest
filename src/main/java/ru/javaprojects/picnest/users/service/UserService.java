package ru.javaprojects.picnest.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.repository.UserRepository;
import ru.javaprojects.picnest.users.to.ProfileTo;
import ru.javaprojects.picnest.users.to.UserTo;
import ru.javaprojects.picnest.users.util.UserUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.javaprojects.picnest.app.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.picnest.users.util.UserUtil.prepareToSave;
import static ru.javaprojects.picnest.users.util.UserUtil.updateFromTo;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final SessionRegistry sessionRegistry;
    private final ChangeEmailService changeEmailService;

    @Value("${content-path.avatars}")
    private String avatarFilesPath;

    public User get(long id) {
        return repository.getExisted(id);
    }

    public User getByEmail(String email) {
        Assert.notNull(email, "email must not be null");
        return repository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new NotFoundException("Not found user with email=" + email, "error.notfound.user", new Object[]{email}));
    }

    public Page<User> getAll(Pageable pageable) {
        Assert.notNull(pageable, "pageable must not be null");
        return repository.findAll(pageable);
    }

    public Page<User> getAllByKeyword(String keyword, Pageable pageable) {
        Assert.notNull(keyword, "keyword must not be null");
        Assert.notNull(pageable, "pageable must not be null");
        return repository.findAllByKeyword(keyword, pageable);
    }

    public Page<ProfileTo> getAllEnabledProfilesByKeyword(String keyword, Pageable pageable) {
        Assert.notNull(keyword, "keyword must not be null");
        Assert.notNull(pageable, "pageable must not be null");
        Page<User> usersPage = repository.findAllEnabledByKeyword(keyword, pageable);
        List<ProfileTo> profiles = UserUtil.asProfileTos(usersPage.getContent());
        return new PageImpl<>(profiles, pageable, usersPage.getTotalElements());
    }

    @Transactional
    public void changePassword(long id, String password) {
        Assert.notNull(password, "password must not be null");
        User user = get(id);
        user.setPassword(PASSWORD_ENCODER.encode(password));
    }

    public void create(User user) {
        Assert.notNull(user, "user must not be null");
        repository.save((User) prepareToSave(user));
    }

    @Transactional
    public void update(UserTo userTo) {
        Assert.notNull(userTo, "userTo must not be null");
        User user = get(userTo.getId());
        String oldEmail = user.getEmail();
        String avatarFileLink = user.getAvatar() != null ? user.getAvatar().getFileLink() : null;
        repository.saveAndFlush(updateFromTo(user, userTo, avatarFilesPath));
        if (!user.getEmail().equalsIgnoreCase(oldEmail) && avatarFileLink != null) {
            FileUtil.moveFile(avatarFileLink, avatarFilesPath + FileUtil.normalizePath(user.getEmail()));
        }
    }

    @Transactional
    public User update(ProfileTo profileTo) {
        Assert.notNull(profileTo, "profileTo must not be null");
        User user = get(profileTo.getId());
        File oldAvatar = user.getAvatar();
        repository.saveAndFlush(updateFromTo(user, profileTo, avatarFilesPath));
        if (profileTo.getAvatar() != null && !profileTo.getAvatar().isEmpty()) {
            FileUtil.upload(profileTo.getAvatar(),  avatarFilesPath + FileUtil.normalizePath(user.getEmail() + "/"),
                    FileUtil.normalizePath(profileTo.getAvatar().getRealFileName()));
            if (oldAvatar != null && !oldAvatar.hasExternalLink() &&
                    !oldAvatar.getFileLink().equalsIgnoreCase(user.getAvatar().getFileLink())) {
                FileUtil.deleteFile(oldAvatar.getFileLink());
            }
        }
        if (!profileTo.getEmail().equalsIgnoreCase(user.getEmail())) {
            changeEmailService.changeEmail(user.id(), profileTo.getEmail());
        }
        return user;
    }

    @Transactional
    public void enable(long id, boolean enabled) {
        User user = get(id);
        user.setEnabled(enabled);
        if (!enabled) {
            expireUserSessions(id);
        }
    }

    @Transactional
    public void delete(long id) {
        User user = get(id);
        repository.delete(user);
        repository.flush();
        expireUserSessions(id);
        if (user.getAvatar() != null) {
            FileUtil.deleteFile(user.getAvatar().getFileLink());
        }
    }

    private void expireUserSessions(long id) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> ((AuthUser) principal).id() == id)
                .findFirst().
                ifPresent(principal -> sessionRegistry.getAllSessions(principal, false)
                        .forEach(SessionInformation::expireNow));
    }

    public Set<Long> getOnlineUsersIds() {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> !sessionRegistry.getAllSessions(principal, false).isEmpty())
                .map(principal -> ((AuthUser) principal).id())
                .collect(Collectors.toSet());
    }

    //TODO use photoService and photo likes
    public int getUserRating(long id) {
        return -1;
    }
}
