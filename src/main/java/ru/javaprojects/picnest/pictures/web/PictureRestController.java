package ru.javaprojects.picnest.pictures.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.validation.NoHtml;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.service.PictureService;
import ru.javaprojects.picnest.pictures.service.StreamBytesInfo;


import java.util.List;

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
        byte[] previewBytes = service.getPicturePreview(id, AuthUser.authId());
        return ResponseEntity.ok(previewBytes);
    }

    @GetMapping("/pictures/{id}/stream")
    public ResponseEntity<StreamingResponseBody> streamVideo(@RequestHeader(value = "Range", required = false) String range,
                                                             @PathVariable("id") Long id) {
        log.info("Stream video for picture with id={}, range=[{}]", id, range);

        List<HttpRange> httpRangeList = HttpRange.parseRanges(range);
        StreamBytesInfo streamBytesInfo = service.getStreamBytes(id, AuthUser.authId(), !httpRangeList.isEmpty() ? httpRangeList.get(0) : null);

        long byteLength = streamBytesInfo.getRangeEnd() - streamBytesInfo.getRangeStart() + 1;
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(!httpRangeList.isEmpty() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                .header("Content-Type", streamBytesInfo.getContentType())
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", Long.toString(byteLength));

        if (!httpRangeList.isEmpty()) {
            builder.header("Content-Range",
                    "bytes " + streamBytesInfo.getRangeStart() +
                            "-" + streamBytesInfo.getRangeEnd() +
                            "/" + streamBytesInfo.getFileSize());
        }
        return builder.body(streamBytesInfo.getResponseBody());
    }
}
