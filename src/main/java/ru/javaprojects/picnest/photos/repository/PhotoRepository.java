package ru.javaprojects.picnest.photos.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.repository.BaseRepository;
import ru.javaprojects.picnest.photos.model.Photo;

import java.util.List;

@Transactional(readOnly = true)
public interface PhotoRepository extends BaseRepository<Photo> {

    List<Photo> findAllByAlbum_Id(long albumId);
}
