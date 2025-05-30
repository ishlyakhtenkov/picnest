package ru.javaprojects.picnest.pictures.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.repository.BaseRepository;
import ru.javaprojects.picnest.pictures.model.Picture;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PictureRepository extends BaseRepository<Picture> {

    List<Picture> findAllByAlbum_Id(long albumId);

    Optional<Picture> findByIdAndOwnerId(long id, long userId);

    @Query("""
            SELECT p.album.id AS albumId, COUNT(p.album.id) AS picturesCount FROM Picture p
            WHERE p.album.id IN :albumsIds GROUP BY p.album.id""")
    List<PictureCount> countPicturesByAlbums(List<Long> albumsIds);


    @Query("""
             SELECT p1.album.id AS albumId, p1 AS pictures FROM Picture p1
             WHERE p1.album.id IN :albumsIds AND p1.created =
            (SELECT MAX(p2.created) FROM Picture p2 WHERE p2.album.id = p1.album.id)""")
    List<AlbumLastPicture> findLastPictureByAlbums(List<Long> albumsIds);


}
