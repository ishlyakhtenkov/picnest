package ru.javaprojects.picnest.pictures.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.repository.BaseRepository;
import ru.javaprojects.picnest.pictures.model.Album;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface AlbumRepository extends BaseRepository<Album> {

    List<Album> findAllByOwner_Id(long userId, Sort s);

    Optional<Album> findByOwner_IdAndNameIgnoreCase(long userId, String name);

    Optional<Album> findByIdAndOwner_Id(long id, long userId);

    @EntityGraph(attributePaths = "owner")
    Optional<Album> findWithOwnerById(long id);

    List<Album> findAllByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "pictures")
    Optional<Album> findWithPicturesByIdAndOwner_Id(long id, long userId, Sort s);
}
