package ru.javaprojects.picnest.pictures.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.error.FileException;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.validation.NoHtml;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.service.PictureService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static ru.javaprojects.picnest.pictures.web.PictureController.ALBUMS_URL;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class PictureRestController {
    private final PictureService service;

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

    @PostMapping(value = ALBUMS_URL + "/{albumId}/pictures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Picture createPicture(@PathVariable long albumId, @RequestPart MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalRequestDataException("Picture file should not be empty",
                    "picture.file-not-empty", null);
        }
        log.info("create picture for album with id={}", albumId);
        return service.createPicture(albumId, file, AuthUser.authId());
    }

    @DeleteMapping("/pictures/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePicture(@PathVariable long id) {
        log.info("delete picture with id={}", id);
        service.deletePicture(id, AuthUser.authId());
    }

    @GetMapping(value = "/pictures/{id}/preview", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPreview(@PathVariable long id) {
        log.info("get preview for picture with id={}", id);
        Picture picture = service.getPicture(id, AuthUser.authId());
        Path filePath = Path.of(picture.getFile().getFileLink());
        var baos = new ByteArrayOutputStream();
        try (var grabber = new FFmpegFrameGrabber(filePath.toString()); var converter = new Java2DFrameConverter()) {
            grabber.start();
            BufferedImage image = converter.convert(grabber.grabImage());
            if (image != null) {
                ImageIO.write(image, "jpeg", baos);
            }
            grabber.stop();
        } catch (IOException e) {
            throw new FileException("Failed to get preview for picture with id=" + id, "picture.preview-failed", null);
        }
        return ResponseEntity.ok(baos.toByteArray());
    }
}
