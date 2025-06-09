package ru.javaprojects.picnest.pictures.service;

import lombok.RequiredArgsConstructor;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.picnest.common.error.FileException;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.model.BaseEntity;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.model.Type;
import ru.javaprojects.picnest.pictures.repository.AlbumLastPicture;
import ru.javaprojects.picnest.pictures.repository.AlbumRepository;
import ru.javaprojects.picnest.pictures.repository.PictureCount;
import ru.javaprojects.picnest.pictures.repository.PictureRepository;
import ru.javaprojects.picnest.users.service.UserService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.bytedeco.ffmpeg.global.avutil.AV_LOG_PANIC;
import static org.bytedeco.ffmpeg.global.avutil.av_log_set_level;

import static ru.javaprojects.picnest.common.util.FileUtil.prepareFileNameToAvoidDuplicate;
import static ru.javaprojects.picnest.pictures.model.Type.IMAGE;
import static ru.javaprojects.picnest.pictures.model.Type.VIDEO;

@Service
@RequiredArgsConstructor
public class PictureService {
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final UserService userService;

    @Value("${content-path.pictures}")
    private String pictureFilesPath;

    public Album getAlbum(long id, long userId, Sort s) {
        return albumRepository.findWithPicturesByIdAndOwner_Id(id, userId, s).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
    }

    public List<Album> getAllAlbumsByOwner(long userId, Sort s) {
        return albumRepository.findAllByOwner_Id(userId, s);
    }

    public Album createAlbum(String name, long userId) {
        Assert.notNull(name, "name must not be null");
        checkDuplicateAlbumName(name, userId);
        return albumRepository.save(new Album(null, name, userService.get(userId)));
    }

    @Transactional
    public void updateAlbum(long id, String name, long userId) {
        Assert.notNull(name, "name must not be null");
        Album album = albumRepository.findByIdAndOwner_Id(id, userId).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
        if (!album.getName().equalsIgnoreCase(name)) {
            checkDuplicateAlbumName(name, userId);
        }
        album.setName(name);
    }

    private void checkDuplicateAlbumName(String name, long userId) {
        albumRepository.findByOwner_IdAndNameIgnoreCase(userId, name).ifPresent((_) -> {
            throw new IllegalRequestDataException("Album with name=" + name + " already exists for user with id=" + userId,
                    "error.duplicate.album-name", null);
        });
    }

    @Transactional
    public void deleteAlbum(long id, long userId) {
        Album album = albumRepository.findByIdAndOwner_Id(id, userId).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
        albumRepository.delete(album);
        albumRepository.flush();
        String albumDir = pictureFilesPath + userId + "/" + id;
        if (Files.exists(Path.of(albumDir))) {
            FileUtil.deleteDirectory(albumDir);
        }
    }

    @Transactional
    public Picture createPicture(long albumId, MultipartFile file, long userId) {
        Assert.notNull(file, "file must not be null");
        Album album = albumRepository.findByIdAndOwner_Id(albumId, userId).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + albumId + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{albumId}));
        String albumDir = pictureFilesPath + userId + "/" + albumId + "/";
        String fileName = prepareFileNameToAvoidDuplicate(albumDir, file.getOriginalFilename());
        String fileLink = albumDir + fileName;
        Type type = determineFileType(file);
        Picture picture = new Picture(null, type, null, null, new File(fileName, fileLink),
                userId, album);
        pictureRepository.saveAndFlush(picture);
        FileUtil.upload(file, albumDir, fileName);
        return picture;
    }

    private Type determineFileType(MultipartFile file) {
        String fileType = file.getContentType();
        Type type;
        if (fileType != null) {
            type = (fileType.startsWith("image/") || file.getOriginalFilename().toLowerCase().endsWith(".heic")) ? IMAGE :
                    fileType.startsWith("video/") ? VIDEO : null;
        } else {
            throw new IllegalRequestDataException("File type is not defined",
                    "picture.file-type-not-defined", null);
        }
        if (type == null) {
            throw new IllegalRequestDataException("Unsupported file type, type=" + fileType,
                    "picture.file-type-not-supported", new Object[]{fileType});
        }
        return type;
    }

    @Transactional
    public void deletePicture(long id, long userId) {
        Picture picture = pictureRepository.findByIdAndOwnerId(id, userId).orElseThrow(() ->
                new NotFoundException("Not found picture with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
        pictureRepository.delete(picture);
        pictureRepository.flush();
        FileUtil.deleteFile(picture.getFile().getFileLink());
    }

    public byte[] getPicturePreview(long id, long userId) {
        Picture picture = pictureRepository.findByIdAndOwnerId(id, userId).orElseThrow(() ->
                new NotFoundException("Not found picture with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
        Path filePath = Path.of(picture.getFile().getFileLink());
        var baos = new ByteArrayOutputStream();
        av_log_set_level(AV_LOG_PANIC);
        try (var grabber = new FFmpegFrameGrabber(filePath.toString()); var converter = new Java2DFrameConverter()) {
            grabber.start();
            BufferedImage image;
            for (int i = 0; i < 50; i++) {
                image = converter.convert(grabber.grabKeyFrame());
                if (image != null) {
                    if (grabber.getDisplayRotation() != 0) {
                        double rads = Math.toRadians(Math.abs(grabber.getDisplayRotation()));
                        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
                        int w = image.getWidth();
                        int h = image.getHeight();
                        int newWidth = (int) Math.floor(w * cos + h * sin);
                        int newHeight = (int) Math.floor(h * cos + w * sin);

                        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = rotated.createGraphics();
                        AffineTransform at = new AffineTransform();
                        at.translate((double) (newWidth - w) / 2, (double) (newHeight - h) / 2);

                        int x = w / 2;
                        int y = h / 2;

                        at.rotate(rads, x, y);
                        g2d.setTransform(at);
                        g2d.drawImage(image, 0, 0, null);
                        g2d.setColor(Color.RED);
                        g2d.drawRect(0, 0, newWidth - 1, newHeight - 1);
                        g2d.dispose();
                        image = rotated;
                    }
                    ImageIO.write(image, "JPEG", baos);
                    break;
                }
            }
            grabber.stop();
        } catch (IOException e) {
            throw new FileException("Failed to get preview for picture with id=" + id, "picture.preview-failed", null);
        }
        return baos.toByteArray();
    }

    public StreamBytesInfo getStreamBytes(long id, long userId, HttpRange range) {
        Picture picture = pictureRepository.findByIdAndOwnerId(id, userId).orElseThrow(() ->
                new NotFoundException("Not found picture with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
        Path filePath = Path.of(picture.getFile().getFileLink());
        try {
            long fileSize = Files.size(filePath);
            long chunkSize = calculateChunkSize(fileSize);
            if (range == null) {
                return new StreamBytesInfo(out -> {
                    try (var is = Files.newInputStream(filePath)) {
                        is.transferTo(out);
                    }
                }, fileSize, 0, fileSize, "video/*");
            }

            long rangeStart = range.getRangeStart(0);
            long rangeEnd = rangeStart + chunkSize;
            if (rangeEnd >= fileSize) {
                rangeEnd = fileSize - 1;
            }
            long finalRangeEnd = rangeEnd;
            return new StreamBytesInfo(out -> {
                try (InputStream inputStream = Files.newInputStream(filePath)) {
                    inputStream.skip(rangeStart);
                    byte[] bytes = inputStream.readNBytes((int) ((finalRangeEnd - rangeStart) + 1));
                    out.write(bytes);
                }
            }, fileSize, rangeStart, rangeEnd, "video/*");
        } catch (IOException ex) {
            throw new FileException("Failed to get stream bytes for picture with id=" + id, "picture.stream-failed", null);
        }
    }

    private long calculateChunkSize(long fileSize) { //TODO
        return fileSize / 5;
    }

    public Map<Long, Integer> countPicturesByAlbums(List<Album> albums) {
        List<Long> albumsIds = albums.stream()
                .map(BaseEntity::getId)
                .toList();
        Map<Long, Integer> picturesCountByAlbums = pictureRepository.countPicturesByAlbums(albumsIds).stream()
                .collect(Collectors.toMap(PictureCount::getAlbumId, PictureCount::getPicturesCount));
        albumsIds.forEach(albumId -> picturesCountByAlbums.computeIfAbsent(albumId, k -> 0));
        return picturesCountByAlbums;
    }

    public Map<Long, Picture> getLastPictureByAlbums(List<Album> albums) {
        List<Long> albumsIds = albums.stream()
                .map(BaseEntity::getId)
                .toList();
        List<AlbumLastPicture> lastPictureByAlbums = pictureRepository.findLastPictureByAlbums(albumsIds);
        Map<Long, Picture> lastPictureByAlbumsMap = new HashMap<>();
        lastPictureByAlbums.forEach(lastPictureByAlbum ->
                lastPictureByAlbumsMap.computeIfAbsent(lastPictureByAlbum.getAlbumId(),
                        _ -> lastPictureByAlbum.getPictures().get(0)));
        return lastPictureByAlbumsMap;
    }
}
