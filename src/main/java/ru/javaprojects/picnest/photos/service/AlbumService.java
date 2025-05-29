package ru.javaprojects.picnest.photos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.photos.model.Album;
import ru.javaprojects.picnest.photos.model.Photo;
import ru.javaprojects.picnest.photos.repository.AlbumRepository;
import ru.javaprojects.picnest.photos.repository.PhotoRepository;
import ru.javaprojects.picnest.users.service.UserService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final UserService userService;

    @Value("${content-path.photos}")
    private String photoFilesPath;

    public Album getAlbum(long id, long userId, Sort s) {
        return albumRepository.findWithPhotosByIdAndOwner_Id(id, userId, s).orElseThrow(() ->
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
        String albumDir = photoFilesPath + userId + "/" + id;
        if (Files.exists(Path.of(albumDir))) {
            FileUtil.deleteDirectory(albumDir);
        }
    }

    @Transactional
    public Photo createPhoto(long albumId, MultipartFile file, long userId) {
        Assert.notNull(file, "file must not be null");
        Album album = albumRepository.findByIdAndOwner_Id(albumId, userId).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + albumId + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{albumId}));
        String albumDir = photoFilesPath + userId + "/" + albumId;
        String fileName = prepareFileName(albumDir, file.getOriginalFilename());
        String fileLink = albumDir + "/" + fileName;
        Photo photo = new Photo(null, null, null, new File(file.getOriginalFilename(), fileLink),
                userId, album);
        photoRepository.saveAndFlush(photo);
        FileUtil.upload(file, albumDir, fileName);
        return photo;
    }

    private String prepareFileName(String albumDir, String fileName) {
        if (Files.exists(Path.of(albumDir, fileName))) {
            String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
            int counter = 1;
            String newFileName;
            do {
                newFileName = fileName.substring(0, fileName.lastIndexOf('.')) + "(" + counter + ")" + fileExtension;
                counter++;
            } while (Files.exists(Path.of(albumDir, newFileName)));
            return newFileName;
        }
        return fileName;
    }
}
