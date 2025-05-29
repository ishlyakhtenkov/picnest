package ru.javaprojects.picnest.photos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.error.NotFoundException;
import ru.javaprojects.picnest.common.util.FileUtil;
import ru.javaprojects.picnest.photos.model.Album;
import ru.javaprojects.picnest.photos.repository.AlbumRepository;
import ru.javaprojects.picnest.users.service.UserService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository repository;
    private final UserService userService;

    @Value("${content-path.photos}")
    private String photoFilesPath;

    public Album get(long id, long userId, Sort s) {
        return repository.findWithPhotosByIdAndOwner_Id(id, userId, s).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
    }

    public List<Album> getAllByOwner(long userId, Sort s) {
        return repository.findAllByOwner_Id(userId, s);
    }

    public Album createAlbum(String name, long userId) {
        Assert.notNull(name, "name must not be null");
        checkDuplicateName(name, userId);
        return repository.save(new Album(null, name, userService.get(userId)));
    }

    @Transactional
    public void updateAlbum(long id, String name, long userId) {
        Assert.notNull(name, "name must not be null");
        Album album = repository.findByIdAndOwner_Id(id, userId).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
        if (!album.getName().equalsIgnoreCase(name)) {
            checkDuplicateName(name, userId);
        }
        album.setName(name);
    }

    private void checkDuplicateName(String name, long userId) {
        repository.findByOwner_IdAndNameIgnoreCase(userId, name).ifPresent((_) -> {
            throw new IllegalRequestDataException("Album with name=" + name + " already exists for user with id=" + userId,
                    "error.duplicate.album-name", null);
        });
    }

    @Transactional
    public void delete(long id, long userId) {
        Album album = repository.findByIdAndOwner_Id(id, userId).orElseThrow(() ->
                new NotFoundException("Not found album with id=" + id + " and userId=" + userId,
                        "error.notfound.entity", new Object[]{id}));
        repository.delete(album);
        repository.flush();
        String albumDir = photoFilesPath + userId + "/" + id;
        if (Files.exists(Path.of(albumDir))) {
            FileUtil.deleteDirectory(albumDir);
        }
    }
}
