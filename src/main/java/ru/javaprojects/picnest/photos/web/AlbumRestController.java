package ru.javaprojects.picnest.photos.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.validation.NoHtml;
import ru.javaprojects.picnest.photos.model.Album;
import ru.javaprojects.picnest.photos.model.Photo;
import ru.javaprojects.picnest.photos.service.AlbumService;

import static ru.javaprojects.picnest.photos.web.AlbumController.ALBUMS_URL;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class AlbumRestController {
    private final AlbumService service;

    @PostMapping(ALBUMS_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public Album createAlbum(@NotBlank(message = "{validation.album.name.NotBlank}")
                             @NoHtml(message = "{validation.album.name.NoHtml}")
                             @Size(max = 4096, message = "{validation.album.name.Size}") @RequestParam String name) {
        log.info("create album '{}' for user with id={}", name, AuthUser.authId());
        return service.createAlbum(name, AuthUser.authId());
    }

    @PutMapping(ALBUMS_URL + "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAlbum(@PathVariable long id,
                            @NotBlank(message = "{validation.album.name.NotBlank}")
                            @NoHtml(message = "{validation.album.name.NoHtml}")
                            @Size(max = 4096, message = "{validation.album.name.Size}") @RequestParam String name) {
        log.info("update album with id={}, new name={}", id, name);
        service.updateAlbum(id, name, AuthUser.authId());
    }

    @DeleteMapping(ALBUMS_URL + "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlbum(@PathVariable long id) {
        log.info("delete album with id={}", id);
        service.deleteAlbum(id, AuthUser.authId());
    }

    @PostMapping(value = ALBUMS_URL + "/{albumId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Photo createPhoto(@PathVariable long albumId, @RequestPart MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalRequestDataException("Photo file should not be empty",
                    "photo.file-not-empty", null);
        }
        log.info("create photo for album with id={}", albumId);
        return service.createPhoto(albumId, file, AuthUser.authId());
    }

    @DeleteMapping("/photos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto(@PathVariable long id) {
        log.info("delete photo with id={}", id);
        service.deletePhoto(id, AuthUser.authId());
    }
}
