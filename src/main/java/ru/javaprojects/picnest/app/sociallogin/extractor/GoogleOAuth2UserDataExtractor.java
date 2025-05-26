package ru.javaprojects.picnest.app.sociallogin.extractor;

public class GoogleOAuth2UserDataExtractor extends OAuth2UserDataExtractor {
    public GoogleOAuth2UserDataExtractor(OAuth2UserData oAuth2UserData) {
        super(oAuth2UserData);
    }

    @Override
    public String getAvatarUrl() {
        return oAuth2UserData.getAttribute("picture");
    }
}
