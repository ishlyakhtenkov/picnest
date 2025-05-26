package ru.javaprojects.picnest.app.sociallogin.extractor;

public class GitHubOAuth2UserDataExtractor extends OAuth2UserDataExtractor {

    public GitHubOAuth2UserDataExtractor(OAuth2UserData oAuth2UserData) {
        super(oAuth2UserData);
    }

    @Override
    public String getName() {
        String name = super.getName();
        return name != null ? name : oAuth2UserData.getAttribute("login");
    }

    @Override
    public String getAvatarUrl() {
        return oAuth2UserData.getAttribute("avatar_url");
    }
}
