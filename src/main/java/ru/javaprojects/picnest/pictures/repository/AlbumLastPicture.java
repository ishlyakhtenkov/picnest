package ru.javaprojects.picnest.pictures.repository;

import ru.javaprojects.picnest.pictures.model.Picture;

import java.util.List;

public interface AlbumLastPicture {
    Long getAlbumId();

    List<Picture> getPictures();
}
