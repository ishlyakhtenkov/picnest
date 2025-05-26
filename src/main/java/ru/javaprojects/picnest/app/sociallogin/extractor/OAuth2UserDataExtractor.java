package ru.javaprojects.picnest.app.sociallogin.extractor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class OAuth2UserDataExtractor {
    protected final OAuth2UserData oAuth2UserData;

    public String getName() {
        return oAuth2UserData.getAttribute("name");
    }

    public String getEmail() {
        return oAuth2UserData.getAttribute("email");
    }

    public abstract String getAvatarUrl();
}
