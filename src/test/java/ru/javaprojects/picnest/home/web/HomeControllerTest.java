package ru.javaprojects.picnest.home.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.pictures.model.Album;

import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.picnest.common.CommonTestData.HOME_URL;
import static ru.javaprojects.picnest.pictures.PictureTestData.*;
import static ru.javaprojects.picnest.users.UserTestData.USER_MAIL;

class HomeControllerTest extends AbstractControllerTest {
    private static final String ABOUT_URL = "/about";
    private static final String CONTACT_URL = "/contact";

    private static final String HOME_VIEW = "home/index";
    private static final String ABOUT_VIEW = "home/about";
    private static final String CONTACT_VIEW = "home/contact";

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePage() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ALBUMS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> ALBUM_MATCHER
                        .assertMatchIgnoreFields((List<Album>) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(ALBUMS_ATTRIBUTE), List.of(userAlbum1, userAlbum2), "owner", "photos"));
    }

    @Test
    void showHomePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist(ALBUMS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW));
    }

    @Test
    void showAboutPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ABOUT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(ABOUT_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAboutPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ABOUT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(ABOUT_VIEW));
    }

    @Test
    void showContactPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTACT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(CONTACT_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showContactPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTACT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(CONTACT_VIEW));
    }
}