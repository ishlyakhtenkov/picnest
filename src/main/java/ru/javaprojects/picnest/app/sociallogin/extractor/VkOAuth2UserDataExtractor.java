package ru.javaprojects.picnest.app.sociallogin.extractor;

import java.util.List;
import java.util.Map;

public class VkOAuth2UserDataExtractor extends OAuth2UserDataExtractor {
    public VkOAuth2UserDataExtractor(OAuth2UserData oAuth2UserData) {
        super(oAuth2UserData);
    }

    @Override
    public String getName() {
        String name = null;
        List<Map<String, Object>> response = oAuth2UserData.getAttribute("response");
        if (response != null) {
            Map<String, Object> attributes = response.get(0);
            if (attributes != null) {
                String firstName =  (String) attributes.get("first_name");
                String lastName =  (String) attributes.get("last_name");
                if (firstName != null) {
                    name = firstName + (lastName != null ? " " + lastName : "");
                }
            }
        }
        return name;
    }

    @Override
    public String getAvatarUrl() {
        return null;
    }
}
