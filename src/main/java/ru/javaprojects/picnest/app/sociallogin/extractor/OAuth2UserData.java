package ru.javaprojects.picnest.app.sociallogin.extractor;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

@AllArgsConstructor
public class OAuth2UserData {
    private final OAuth2User oAuth2User;
    private final OAuth2UserRequest oAuth2UserRequest;

    @Nullable
    @SuppressWarnings("unchecked")
    public <A> A getAttribute(String attributeName) {
        A attribute = oAuth2User.getAttribute(attributeName);
        return attribute != null ? attribute : (A) oAuth2UserRequest.getAdditionalParameters().get(attributeName);
    }
}