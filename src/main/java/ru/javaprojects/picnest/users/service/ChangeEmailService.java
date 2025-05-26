package ru.javaprojects.picnest.users.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.mail.MailSender;
import ru.javaprojects.picnest.users.error.TokenException;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.model.token.ChangeEmailToken;
import ru.javaprojects.picnest.users.repository.ChangeEmailTokenRepository;
import ru.javaprojects.picnest.users.repository.UserRepository;
import ru.javaprojects.picnest.users.to.UserTo;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MILLIS;

@Service
public class ChangeEmailService extends TokenService<ChangeEmailToken> {
    private UserService userService;

    public ChangeEmailService(MailSender mailSender, MessageSource messageSource, UserRepository userRepository,
                              @Value("${change-email.token-expiration-time}") long tokenExpirationTime,
                              @Value("${change-email.confirm-url}") String confirmUrl,
                              ChangeEmailTokenRepository tokenRepository) {
        super(mailSender, messageSource, userRepository, tokenRepository, tokenExpirationTime, confirmUrl, "change-email");
    }

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Transactional
    public void changeEmail(long userId, String newEmail) {
        Assert.notNull(newEmail, "newEmail must not be null");
        userRepository.findByEmailIgnoreCase(newEmail).ifPresent(user -> {
            String exMessage = user.id() == userId ? ("user with id=" + userId + " already has email=" + newEmail) :
                    ("email=" + newEmail + " already in use");
            String exMessageCode = user.id() == userId ? "change-email.already-has-email" : "change-email.already-in-use";
            throw new IllegalRequestDataException(exMessage, exMessageCode, new Object[]{newEmail});
        });
        var changeEmailToken = ((ChangeEmailTokenRepository) tokenRepository).findByUser_Id(userId)
                .orElseGet(() -> new ChangeEmailToken(null, UUID.randomUUID().toString(),
                        LocalDateTime.now().plus(tokenExpirationTime, MILLIS), newEmail, userRepository.getExisted(userId)));
        if (!changeEmailToken.isNew()) {
            changeEmailToken.setToken(UUID.randomUUID().toString());
            changeEmailToken.setExpiryTimestamp(LocalDateTime.now().plus(tokenExpirationTime, MILLIS));
            changeEmailToken.setNewEmail(newEmail);
        }
        tokenRepository.saveAndFlush(changeEmailToken);
        sendEmail(newEmail, changeEmailToken.getToken());
    }

    @Transactional
    public void confirmChangeEmail(String token, long userId) {
        Assert.notNull(token, "token must not be null");
        var changeEmailToken = getAndCheckToken(token);
        User user = changeEmailToken.getUser();
        if (user.id() != userId) {
            throw new TokenException("token " + token + " not belongs to user with id=" + userId,
                    "change-email.token-not-belongs", null);
        }
        userService.update(new UserTo(user.getId(), changeEmailToken.getNewEmail(), user.getName(), user.getRoles()));
        tokenRepository.delete(changeEmailToken);
    }
}
