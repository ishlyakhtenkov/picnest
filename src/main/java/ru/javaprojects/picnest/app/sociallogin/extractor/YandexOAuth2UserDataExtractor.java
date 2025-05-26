package ru.javaprojects.picnest.app.sociallogin.extractor;

public class YandexOAuth2UserDataExtractor extends OAuth2UserDataExtractor {
    public YandexOAuth2UserDataExtractor(OAuth2UserData oAuth2UserData) {
        super(oAuth2UserData);
    }

    @Override
    public String getName() {
        String name = oAuth2UserData.getAttribute("real_name");
        return name != null ? name : oAuth2UserData.getAttribute("login");
    }

    @Override
    public String getEmail() {
        return oAuth2UserData.getAttribute("default_email");
    }

    @Override
    public String getAvatarUrl() {
        String avatarId = oAuth2UserData.getAttribute("default_avatar_id");
        return avatarId != null ? String.format("https://avatars.yandex.net/get-yapic/%s/islands-200", avatarId) : null;
    }
}
