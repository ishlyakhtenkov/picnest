package ru.javaprojects.picnest.pictures.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.repository.BaseRepository;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Photo;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PhotoRepository extends BaseRepository<Photo> {

    List<Photo> findAllByAlbum_Id(long albumId);

    Optional<Photo> findByIdAndOwnerId(long id, long userId);
}
