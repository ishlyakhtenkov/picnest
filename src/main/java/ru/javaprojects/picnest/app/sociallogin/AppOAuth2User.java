package ru.javaprojects.picnest.app.sociallogin;

import lombok.NonNull;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.users.model.User;

import java.util.Map;

public class AppOAuth2User extends AuthUser implements OAuth2User {
    private final OAuth2User oauth2User;

    public AppOAuth2User(@NonNull OAuth2User oauth2User, @NonNull User user) {
        super(user);
        this.oauth2User = oauth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public String getName() {
        return super.getUsername();
    }
}
