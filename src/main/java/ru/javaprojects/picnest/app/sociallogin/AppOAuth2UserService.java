package ru.javaprojects.picnest.app.sociallogin;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Service;
import ru.javaprojects.picnest.app.sociallogin.extractor.*;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.users.model.Role;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.repository.UserRepository;

import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository repository;

    @Override
    public org.springframework.security.oauth2.core.user.OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var oAuth2User = super.loadUser(userRequest);
        String clientRegistrationId = userRequest.getClientRegistration().getRegistrationId();
        var oAuth2UserDataExtractor = getExtractor(clientRegistrationId, new OAuth2UserData(oAuth2User, userRequest));
        String email = oAuth2UserDataExtractor.getEmail();
        String name = oAuth2UserDataExtractor.getName();
        if (email == null || name == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN),
                    clientRegistrationId + " account does not contain email or name");
        }
        var user = new AppOAuth2User(oAuth2User, repository.findByEmailIgnoreCase(email.toLowerCase())
                .orElseGet(() -> {
                    String avatarUrl = oAuth2UserDataExtractor.getAvatarUrl();
                    File avatar = null;
                    if (avatarUrl != null) {
                        avatar = new File(clientRegistrationId + "_oAuth2_avatar", avatarUrl);
                    }
                    return repository.save(new User(null, email, name, null, UUID.randomUUID().toString(), true,
                            Set.of(Role.USER), avatar));
                }));
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        return user;
    }

    private OAuth2UserDataExtractor getExtractor(String clientRegistrationId, OAuth2UserData oAuth2UserData) {
        return switch (clientRegistrationId) {
            case "google" -> new GoogleOAuth2UserDataExtractor(oAuth2UserData);
            case "github" -> new GitHubOAuth2UserDataExtractor(oAuth2UserData);
            case "yandex" -> new YandexOAuth2UserDataExtractor(oAuth2UserData);
            case "gitlab" -> new GitLabOAuth2UserDataExtractor(oAuth2UserData);
            case "vk" -> new VkOAuth2UserDataExtractor(oAuth2UserData);
            default -> throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT),
                    "Unknown provider: " + clientRegistrationId);
        };
    }
}
