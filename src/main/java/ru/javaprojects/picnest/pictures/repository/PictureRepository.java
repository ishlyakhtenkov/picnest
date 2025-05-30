package ru.javaprojects.picnest.pictures.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.repository.BaseRepository;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.model.Picture;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PictureRepository extends BaseRepository<Picture> {

    List<Picture> findAllByAlbum_Id(long albumId);

    Optional<Picture> findByIdAndOwnerId(long id, long userId);
}
