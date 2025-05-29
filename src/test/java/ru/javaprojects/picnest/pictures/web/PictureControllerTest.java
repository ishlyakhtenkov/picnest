package ru.javaprojects.picnest.pictures.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.ContentFilesManager;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.pictures.model.Album;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.picnest.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.picnest.common.CommonTestData.NOT_EXISTING_ID;
import static ru.javaprojects.picnest.pictures.PictureTestData.*;
import static ru.javaprojects.picnest.pictures.web.PictureRestControllerTest.ALBUMS_URL_SLASH;
import static ru.javaprojects.picnest.users.UserTestData.ADMIN_MAIL;
import static ru.javaprojects.picnest.users.UserTestData.USER_MAIL;
import static ru.javaprojects.picnest.users.web.LoginController.LOGIN_URL;

class PictureControllerTest extends AbstractControllerTest implements ContentFilesManager {
    private static final String ALBUM_VIEW = "photos/album";

    @Value("${content-path.pictures}")
    private String pictureFilesPath;

    @Override
    public Path getContentPath() {
        return Paths.get(pictureFilesPath);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(PICTURES_TEST_CONTENT_FILES_PATH);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showAlbumPage() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL_SLASH + USER_ALBUM1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ALBUM_ATTRIBUTE))
                .andExpect(view().name(ALBUM_VIEW))
                .andExpect(result -> ALBUM_MATCHER
                        .assertMatchIgnoreFields((Album) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(ALBUM_ATTRIBUTE), userAlbum1, "owner", "photos.album"));
    }

    @Test
    void showAlbumPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL_SLASH + USER_ALBUM1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAlbumPageNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL_SLASH + NOT_EXISTING_ID))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{NOT_EXISTING_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAlbumPageNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL_SLASH + ADMIN_ALBUM1_ID))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{ADMIN_ALBUM1_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAlbumPageNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL_SLASH + USER_ALBUM1_ID))
                .andExpect(exception().message(messageSource.getMessage("error.internal-server-error", null, getLocale()),
                        messageSource.getMessage("error.notfound.entity", new Object[]{USER_ALBUM1_ID}, getLocale()),
                        NotFoundException.class));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showPhotoByUrl() throws Exception {
        perform(MockMvcRequestBuilders.get(userAlbum1Photo1.getFile().getFileLink().substring(1)))
                .andExpect(status().isOk());
    }

    @Test
    void showPhotoByUrlUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(userAlbum1Photo1.getFile().getFileLink().substring(1)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showPhotoByUrlNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(adminAlbum1Photo1.getFile().getFileLink().substring(1)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showPhotoByUrlNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.get(userAlbum1Photo1.getFile().getFileLink().substring(1)))
                .andExpect(status().isInternalServerError());
    }
}