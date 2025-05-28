package ru.javaprojects.picnest.photos.repository;

import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.repository.BaseRepository;
import ru.javaprojects.picnest.photos.model.Album;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface AlbumRepository extends BaseRepository<Album> {

    List<Album> findAllByOwner_Id(long userId, Sort s);

    Optional<Album> findByOwner_IdAndNameIgnoreCase(long userId, String name);

    Optional<Album> findByIdAndOwner_Id(long id, long userId);
}
