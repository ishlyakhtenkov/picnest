package ru.javaprojects.picnest.photos.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.ContentFilesManager;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.photos.model.Album;
import ru.javaprojects.picnest.photos.repository.AlbumRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.picnest.common.CommonTestData.*;
import static ru.javaprojects.picnest.photos.AlbumTestData.*;
import static ru.javaprojects.picnest.photos.web.AlbumRestController.ALBUMS_URL;
import static ru.javaprojects.picnest.users.UserTestData.*;
import static ru.javaprojects.picnest.users.web.LoginController.LOGIN_URL;

class AlbumRestControllerTest extends AbstractControllerTest implements ContentFilesManager {
    private static final String ALBUMS_URL_SLASH = ALBUMS_URL + "/";

    @Value("${content-path.photos}")
    private String photoFilesPath;

    @Autowired
    private AlbumRepository repository;

    @Override
    public Path getContentPath() {
        return Paths.get(photoFilesPath);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(ALBUMS_TEST_CONTENT_FILES_PATH);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createAlbum() throws Exception {
        Album newAlbum = getNewAlbum();
        ResultActions action = perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .param(NAME_PARAM, newAlbum.getName())
                .with(csrf()))
                .andExpect(status().isCreated());
        Album created = ALBUM_MATCHER.readFromJson(action);
        newAlbum.setId(created.getId());
        ALBUM_MATCHER.assertMatch(created, newAlbum);
        ALBUM_MATCHER.assertMatch(repository.findWithOwnerById(created.id()).orElseThrow(), newAlbum);
    }

    @Test
    void createAlbumUnauthorized() throws Exception {
        Album newAlbum = getNewAlbum();
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .param(NAME_PARAM, newAlbum.getName())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(repository.findAllByNameIgnoreCase(newAlbum.getName()).isEmpty());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createAlbumInvalid() throws Exception {
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .param(NAME_PARAM, HTML_TEXT)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(ConstraintViolationException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("createAlbum.name: Album name must not be HTML"))
                .andExpect(jsonPath("$.invalid_params")
                        .value(messageSource.getMessage("validation.album.name.NoHtml", null, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL));
        assertTrue(repository.findAllByNameIgnoreCase(HTML_TEXT).isEmpty());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createAlbumDuplicateName() throws Exception {
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .param(NAME_PARAM, userAlbum1.getName())
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(IllegalRequestDataException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.duplicate.album-name", null, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL));
        assertEquals(1, repository.findAllByNameIgnoreCase(userAlbum1.getName()).size());
        ALBUM_MATCHER.assertMatchIgnoreFields(repository.findAllByNameIgnoreCase(userAlbum1.getName()).get(0), userAlbum1,
                "owner", "updated");
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createAlbumWhenAnotherUserAlbumHasSuchName() throws Exception {
        Album newAlbum = getNewAlbum();
        newAlbum.setName(adminAlbum1.getName());
        ResultActions action = perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .param(NAME_PARAM, adminAlbum1.getName())
                .with(csrf()))
                .andExpect(status().isCreated());
        Album created = ALBUM_MATCHER.readFromJson(action);
        newAlbum.setId(created.getId());
        ALBUM_MATCHER.assertMatch(created, newAlbum);
        ALBUM_MATCHER.assertMatch(repository.findWithOwnerById(created.id()).orElseThrow(), newAlbum);
        assertEquals(2, repository.findAllByNameIgnoreCase(adminAlbum1.getName()).size());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAlbum() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, getUpdated().getName())
                .with(csrf()))
                .andExpect(status().isNoContent());
        Album updated = repository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow();
        ALBUM_MATCHER.assertMatch(updated, getUpdated());
    }

    @Test
    void updateAlbumUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, getUpdated().getName())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(getUpdated().getName(), repository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER.assertMatch(repository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAlbumNotFound() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + NOT_EXISTING_ID)
                .param(NAME_PARAM, getUpdated().getName())
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + NOT_EXISTING_ID));
        assertThrows(NotFoundException.class, () -> repository.getExisted(NOT_EXISTING_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAlbumInvalid() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, HTML_TEXT)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(ConstraintViolationException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("updateAlbum.name: Album name must not be HTML"))
                .andExpect(jsonPath("$.invalid_params")
                        .value(messageSource.getMessage("validation.album.name.NoHtml", null, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + USER_ALBUM1_ID));
        assertNotEquals(HTML_TEXT, repository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER.assertMatch(repository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);

    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAlbumDuplicateName() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, userAlbum2.getName())
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(IllegalRequestDataException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.duplicate.album-name", null, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + USER_ALBUM1_ID));
        assertNotEquals(userAlbum2.getName(), repository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER.assertMatch(repository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAlbumWhenAnotherUserAlbumHasSuchName() throws Exception {
        Album updatedAlbum = getUpdated();
        updatedAlbum.setName(adminAlbum1.getName());
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, updatedAlbum.getName())
                .with(csrf()))
                .andExpect(status().isNoContent());
        Album updated = repository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow();
        ALBUM_MATCHER.assertMatch(updated, updatedAlbum);
        assertEquals(2, repository.findAllByNameIgnoreCase(updatedAlbum.getName()).size());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAlbumNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + ADMIN_ALBUM1_ID)
                .param(NAME_PARAM, getUpdated().getName())
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{ADMIN_ALBUM1_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + ADMIN_ALBUM1_ID));
        assertNotEquals(getUpdated().getName(), repository.getExisted(ADMIN_ALBUM1_ID).getName());
        ALBUM_MATCHER.assertMatch(repository.findWithOwnerById(ADMIN_ALBUM1_ID).orElseThrow(), adminAlbum1);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateAlbumNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, getUpdated().getName())
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{USER_ALBUM1_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + USER_ALBUM1_ID));
        assertNotEquals(getUpdated().getName(), repository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER.assertMatch(repository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteAlbum() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> repository.getExisted(USER_ALBUM1_ID));
        assertTrue(Files.notExists(Paths.get(photoFilesPath + USER_ID + "/" + USER_ALBUM1_ID)));
    }

    @Test
    void deleteAlbumUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .with(csrf()))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> repository.getExisted(USER_ALBUM1_ID));
        assertTrue(Files.exists(Paths.get(photoFilesPath + USER_ID + "/" + USER_ALBUM1_ID)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteAlbumNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + NOT_EXISTING_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteAlbumNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + ADMIN_ALBUM1_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{ADMIN_ALBUM1_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + ADMIN_ALBUM1_ID));
        assertDoesNotThrow(() -> repository.getExisted(ADMIN_ALBUM1_ID));
        assertTrue(Files.exists(Paths.get(photoFilesPath + ADMIN_ID + "/" + ADMIN_ALBUM1_ID)));

    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteAlbumNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{USER_ALBUM1_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + USER_ALBUM1_ID));
        assertDoesNotThrow(() -> repository.getExisted(USER_ALBUM1_ID));
        assertTrue(Files.exists(Paths.get(photoFilesPath + USER_ID + "/" + USER_ALBUM1_ID)));
    }
}