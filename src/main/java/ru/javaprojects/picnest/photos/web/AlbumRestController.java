package ru.javaprojects.picnest.photos.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.validation.NoHtml;
import ru.javaprojects.picnest.photos.model.Album;
import ru.javaprojects.picnest.photos.service.AlbumService;

@RestController
@RequestMapping(value = AlbumController.ALBUMS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class AlbumRestController {
    private final AlbumService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Album createAlbum(@NotBlank(message = "{validation.album.name.NotBlank}")
                             @NoHtml(message = "{validation.album.name.NoHtml}")
                             @Size(max = 4096, message = "{validation.album.name.Size}") @RequestParam String name) {
        log.info("create album '{}' for user with id={}", name, AuthUser.authId());
        return service.createAlbum(name, AuthUser.authId());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAlbum(@PathVariable long id,
                            @NotBlank(message = "{validation.album.name.NotBlank}")
                            @NoHtml(message = "{validation.album.name.NoHtml}")
                            @Size(max = 4096, message = "{validation.album.name.Size}") @RequestParam String name) {
        log.info("update album with id={}, new name={}", id, name);
        service.updateAlbum(id, name, AuthUser.authId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete album with id={}", id);
        service.delete(id, AuthUser.authId());
    }
}
