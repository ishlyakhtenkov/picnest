package ru.javaprojects.picnest.home.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.photos.model.Album;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.picnest.common.CommonTestData.HOME_URL;
import static ru.javaprojects.picnest.photos.AlbumTestData.*;
import static ru.javaprojects.picnest.users.UserTestData.USER_MAIL;

class HomeControllerTest extends AbstractControllerTest {
    private static final String ALBUMS_ATTRIBUTE = "albums";
    private static final String HOME_VIEW = "home/index";

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
                                .getModel().get(ALBUMS_ATTRIBUTE), List.of(userAlbum1, userAlbum2), "owner"));
    }

    @Test
    void showHomePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist(ALBUMS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW));
    }
}