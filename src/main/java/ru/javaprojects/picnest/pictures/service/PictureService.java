package ru.javaprojects.picnest.pictures.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.model.BaseEntity;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.repository.AlbumRepository;
import ru.javaprojects.picnest.pictures.repository.PictureCount;
import ru.javaprojects.picnest.pictures.repository.PictureRepository;
import ru.javaprojects.picnest.users.service.UserService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.javaprojects.picnest.common.util.FileUtil.prepareFileNameToAvoidDuplicate;

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
        Picture picture = new Picture(null, null, null, new File(fileName, fileLink),
                userId, album);
        pictureRepository.saveAndFlush(picture);
        FileUtil.upload(file, albumDir, fileName);
        return picture;
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

    public Map<Long, Integer> countPicturesByAlbums(List<Album> albums) {
        List<Long> albumsIds = albums.stream()
                .map(BaseEntity::getId)
                .toList();
        Map<Long, Integer> picturesCountByAlbums = pictureRepository.countPicturesByAlbums(albumsIds).stream()
                .collect(Collectors.toMap(PictureCount::getAlbumId, PictureCount::getPicturesCount));
        albumsIds.forEach(albumId -> picturesCountByAlbums.computeIfAbsent(albumId, k -> 0));
        return picturesCountByAlbums;
    }


}
