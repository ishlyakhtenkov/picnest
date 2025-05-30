package ru.javaprojects.picnest.pictures.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.picnest.AbstractControllerTest;
import ru.javaprojects.picnest.ContentFilesManager;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.repository.AlbumRepository;
import ru.javaprojects.picnest.pictures.repository.PictureRepository;

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
import static ru.javaprojects.picnest.pictures.PictureTestData.*;
import static ru.javaprojects.picnest.pictures.web.PictureController.ALBUMS_URL;
import static ru.javaprojects.picnest.users.UserTestData.*;
import static ru.javaprojects.picnest.users.web.LoginController.LOGIN_URL;

class PictureRestControllerTest extends AbstractControllerTest implements ContentFilesManager {
    static final String ALBUMS_URL_SLASH = ALBUMS_URL + "/";
    static final String PICTURES_URL_SLASH = "/pictures/";

    @Value("${content-path.pictures}")
    private String pictureFilesPath;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PictureRepository pictureRepository;

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
    void createAlbum() throws Exception {
        Album newAlbum = getNewAlbum();
        ResultActions action = perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .param(NAME_PARAM, newAlbum.getName())
                .with(csrf()))
                .andExpect(status().isCreated());
        Album created = ALBUM_MATCHER.readFromJson(action);
        newAlbum.setId(created.getId());
        ALBUM_MATCHER.assertMatch(created, newAlbum);
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(albumRepository.findWithOwnerById(created.id()).orElseThrow(), newAlbum);
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
        assertTrue(albumRepository.findAllByNameIgnoreCase(newAlbum.getName()).isEmpty());
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
        assertTrue(albumRepository.findAllByNameIgnoreCase(HTML_TEXT).isEmpty());
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
        assertEquals(1, albumRepository.findAllByNameIgnoreCase(userAlbum1.getName()).size());
        ALBUM_MATCHER.assertMatchIgnoreFields(albumRepository.findAllByNameIgnoreCase(userAlbum1.getName()).get(0), userAlbum1,
                "owner", "updated", "pictures");
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
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(albumRepository.findWithOwnerById(created.id()).orElseThrow(), newAlbum);
        assertEquals(2, albumRepository.findAllByNameIgnoreCase(adminAlbum1.getName()).size());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateAlbum() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, getUpdated().getName())
                .with(csrf()))
                .andExpect(status().isNoContent());
        Album updated = albumRepository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow();
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(updated, getUpdated());
    }

    @Test
    void updateAlbumUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.put(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .param(NAME_PARAM, getUpdated().getName())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(getUpdated().getName(), albumRepository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(albumRepository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);
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
        assertThrows(NotFoundException.class, () -> albumRepository.getExisted(NOT_EXISTING_ID));
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
        assertNotEquals(HTML_TEXT, albumRepository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(albumRepository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);

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
        assertNotEquals(userAlbum2.getName(), albumRepository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(albumRepository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);
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
        Album updated = albumRepository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow();
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(updated, updatedAlbum);
        assertEquals(2, albumRepository.findAllByNameIgnoreCase(updatedAlbum.getName()).size());
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
        assertNotEquals(getUpdated().getName(), albumRepository.getExisted(ADMIN_ALBUM1_ID).getName());
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(albumRepository.findWithOwnerById(ADMIN_ALBUM1_ID).orElseThrow(), adminAlbum1);
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
        assertNotEquals(getUpdated().getName(), albumRepository.getExisted(USER_ALBUM1_ID).getName());
        ALBUM_MATCHER_EXCLUDE_PICTURES.assertMatch(albumRepository.findWithOwnerById(USER_ALBUM1_ID).orElseThrow(), userAlbum1);
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteAlbum() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> albumRepository.getExisted(USER_ALBUM1_ID));
        assertTrue(Files.notExists(Paths.get(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID))));
    }

    @Test
    void deleteAlbumUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + USER_ALBUM1_ID)
                .with(csrf()))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> albumRepository.getExisted(USER_ALBUM1_ID));
        assertTrue(Files.exists(Paths.get(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID))));
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
        assertDoesNotThrow(() -> albumRepository.getExisted(ADMIN_ALBUM1_ID));
        assertTrue(Files.exists(Paths.get(pictureFilesPath, String.valueOf(ADMIN_ID), String.valueOf(ADMIN_ALBUM1_ID))));
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
        assertDoesNotThrow(() -> albumRepository.getExisted(USER_ALBUM1_ID));
        assertTrue(Files.exists(Paths.get(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID))));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createPicture() throws Exception {
        Picture newPicture = getNewPhoto();
        ResultActions action = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ALBUMS_URL_SLASH + USER_ALBUM1_ID + "/pictures")
                .file(NEW_PHOTO_FILE)
                .with(csrf()))
                .andExpect(status().isCreated());
        Picture created = PICTURE_MATCHER.readFromJson(action);
        newPicture.setId(created.getId());
        PICTURE_MATCHER.assertMatchIgnoreFields(created, newPicture, "created", "album.pictures", "album.owner");
        PICTURE_MATCHER.assertMatchIgnoreFields(pictureRepository.getExisted(created.id()), newPicture, "created", "album");
        assertTrue(Files.exists(Path.of(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), NEW_PHOTO_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createPictureWhenDuplicateFileName() throws Exception {
        Picture newPicture = getNewPhoto();
        newPicture.getFile().setFileName("ph1(1).jpg");
        newPicture.getFile().setFileLink(pictureFilesPath + USER_ID + "/" + USER_ALBUM1_ID + "/" + "ph1(1).jpg");
        ResultActions action = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ALBUMS_URL_SLASH + USER_ALBUM1_ID + "/pictures")
                .file(DUPLICATE_NAME_NEW_PHOTO_FILE)
                .with(csrf()))
                .andExpect(status().isCreated());
        Picture created = PICTURE_MATCHER.readFromJson(action);
        newPicture.setId(created.getId());
        PICTURE_MATCHER.assertMatchIgnoreFields(created, newPicture, "created", "album.pictures", "album.owner");
        PICTURE_MATCHER.assertMatchIgnoreFields(pictureRepository.getExisted(created.id()), newPicture, "created", "album");
        assertTrue(Files.exists(Path.of(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), "ph1(1).jpg")));
        assertTrue(Files.exists(Path.of(USER_ALBUM_1_PICTURE_1.getFile().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createPictureWhenEmptyFile() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ALBUMS_URL_SLASH + USER_ALBUM1_ID + "/pictures")
                .file(EMPTY_PHOTO_FILE)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(IllegalRequestDataException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(messageSource.getMessage("picture.file-not-empty", null, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + USER_ALBUM1_ID + "/pictures"));
        assertEquals(userAlbum1.getPictures().size(),
                albumRepository.findWithPicturesByIdAndOwner_Id(USER_ALBUM1_ID, USER_ID, Sort.unsorted()).orElseThrow().getPictures().size());
        assertTrue(Files.notExists(Path.of(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), EMPTY_PHOTO_FILE.getOriginalFilename())));
    }

    @Test
    void createPictureUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ALBUMS_URL_SLASH + USER_ALBUM1_ID + "/pictures")
                .file(NEW_PHOTO_FILE)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertEquals(userAlbum1.getPictures().size(),
                albumRepository.findWithPicturesByIdAndOwner_Id(USER_ALBUM1_ID, USER_ID, Sort.unsorted()).orElseThrow().getPictures().size());
        assertTrue(Files.notExists(Path.of(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), NEW_PHOTO_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createPictureWhenAlbumNotExists() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ALBUMS_URL_SLASH + NOT_EXISTING_ID + "/pictures")
                .file(NEW_PHOTO_FILE)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + NOT_EXISTING_ID + "/pictures"));
        assertTrue(Files.notExists(Path.of(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(NOT_EXISTING_ID), NEW_PHOTO_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createPictureWhenAlbumNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ALBUMS_URL_SLASH + ADMIN_ALBUM1_ID + "/pictures")
                .file(NEW_PHOTO_FILE)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{ADMIN_ALBUM1_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + ADMIN_ALBUM1_ID + "/pictures"));
        assertEquals(adminAlbum1.getPictures().size(),
                albumRepository.findWithPicturesByIdAndOwner_Id(ADMIN_ALBUM1_ID, ADMIN_ID, Sort.unsorted()).orElseThrow().getPictures().size());
        assertTrue(Files.notExists(Path.of(pictureFilesPath, String.valueOf(ADMIN_ID), String.valueOf(ADMIN_ALBUM1_ID), NEW_PHOTO_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createPictureWhenAlbumNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, ALBUMS_URL_SLASH + USER_ALBUM1_ID + "/pictures")
                .file(NEW_PHOTO_FILE)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{USER_ALBUM1_ID}, getLocale())))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + USER_ALBUM1_ID + "/pictures"));
        assertEquals(userAlbum1.getPictures().size(),
                albumRepository.findWithPicturesByIdAndOwner_Id(USER_ALBUM1_ID, USER_ID, Sort.unsorted()).orElseThrow().getPictures().size());
        assertTrue(Files.notExists(Path.of(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), NEW_PHOTO_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deletePicture() throws Exception {
        perform(MockMvcRequestBuilders.delete(PICTURES_URL_SLASH + USER_ALBUM1_PHOTO1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> pictureRepository.getExisted(USER_ALBUM1_PHOTO1_ID));
        assertTrue(Files.notExists(Paths.get(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), USER_ALBUM_1_PICTURE_1.getFile().getFileName())));
    }

    @Test
    void deletePictureUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(PICTURES_URL_SLASH + USER_ALBUM1_PHOTO1_ID)
                .with(csrf()))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> pictureRepository.getExisted(USER_ALBUM1_PHOTO1_ID));
        assertTrue(Files.exists(Paths.get(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), USER_ALBUM_1_PICTURE_1.getFile().getFileName())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deletePictureNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(PICTURES_URL_SLASH + NOT_EXISTING_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{NOT_EXISTING_ID}, getLocale())))
                .andExpect(problemInstance(PICTURES_URL_SLASH + NOT_EXISTING_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deletePictureNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.delete(PICTURES_URL_SLASH + ADMIN_ALBUM1_PHOTO1_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{ADMIN_ALBUM1_PHOTO1_ID}, getLocale())))
                .andExpect(problemInstance(PICTURES_URL_SLASH + ADMIN_ALBUM1_PHOTO1_ID));
        assertDoesNotThrow(() -> pictureRepository.getExisted(ADMIN_ALBUM1_PHOTO1_ID));
        assertTrue(Files.exists(Paths.get(pictureFilesPath, String.valueOf(ADMIN_ID), String.valueOf(ADMIN_ALBUM1_ID), ADMIN_ALBUM_1_PICTURE_1.getFile().getFileName())));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deletePictureNotBelongsByAdmin() throws Exception {
        perform(MockMvcRequestBuilders.delete(PICTURES_URL_SLASH + USER_ALBUM1_PHOTO1_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(NotFoundException.class,
                        Objects.requireNonNull(result.getResolvedException()).getClass()))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(messageSource.getMessage("error.notfound.entity",
                        new Object[]{USER_ALBUM1_PHOTO1_ID}, getLocale())))
                .andExpect(problemInstance(PICTURES_URL_SLASH + USER_ALBUM1_PHOTO1_ID));
        assertDoesNotThrow(() -> pictureRepository.getExisted(USER_ALBUM1_PHOTO1_ID));
        assertTrue(Files.exists(Paths.get(pictureFilesPath, String.valueOf(USER_ID), String.valueOf(USER_ALBUM1_ID), USER_ALBUM_1_PICTURE_1.getFile().getFileName())));
    }
}