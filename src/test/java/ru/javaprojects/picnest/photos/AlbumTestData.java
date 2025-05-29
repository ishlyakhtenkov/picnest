package ru.javaprojects.picnest.photos;

import ru.javaprojects.picnest.MatcherFactory;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.photos.model.Album;
import ru.javaprojects.picnest.photos.model.Photo;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static ru.javaprojects.picnest.users.UserTestData.admin;
import static ru.javaprojects.picnest.users.UserTestData.user;

public class AlbumTestData {
    public static final MatcherFactory.Matcher<Album> ALBUM_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Album.class, "created", "updated", "owner.password",
                    "owner.registered", "owner.roles");

    public static final MatcherFactory.Matcher<Album> ALBUM_MATCHER_EXCLUDE_PHOTOS =
            MatcherFactory.usingIgnoringFieldsComparator(Album.class, "created", "updated", "owner.password",
                    "owner.registered", "owner.roles", "photos");

    public static final String ALBUMS_TEST_CONTENT_FILES_PATH = "src/test/test-content-files/photos";

    public static final long USER_ALBUM1_ID = 100011;
    public static final long USER_ALBUM2_ID = 100012;
    public static final long ADMIN_ALBUM1_ID = 100013;

    public static final String ALBUM_ATTRIBUTE = "album";
    public static final String ALBUMS_ATTRIBUTE = "albums";

    public static final long USER_ALBUM1_PHOTO1_ID = 100014;
    public static final long USER_ALBUM1_PHOTO2_ID = 100015;
    public static final long USER_ALBUM1_PHOTO3_ID = 100016;

    public static final long ADMIN_ALBUM1_PHOTO1_ID = 100018;

    public static final Photo userAlbum1Photo1 = new Photo(USER_ALBUM1_PHOTO1_ID, "photo 1 user alb 1 desc",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 3),
            new File("ph1.jpg", "./picnest/content/photos/100000/100011/ph1.jpg"));

    public static final Photo userAlbum1Photo2 = new Photo(USER_ALBUM1_PHOTO2_ID, "photo 2 user alb 1 desc",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 7),
            new File("ph2.jpg", "./picnest/content/photos/100000/100011/ph2.jpg"));

    public static final Photo userAlbum1Photo3 = new Photo(USER_ALBUM1_PHOTO3_ID, "photo 3 user alb 1 desc",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 9),
            new File("ph3.jpg", "./picnest/content/photos/100000/100011/ph3.jpg"));

    public static final Album userAlbum1 = new Album(USER_ALBUM1_ID, "user album 1",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 1), user,
            List.of(userAlbum1Photo3, userAlbum1Photo2, userAlbum1Photo1));

    public static final Album userAlbum2 = new Album(USER_ALBUM2_ID, "user album 2",
            LocalDateTime.of(2025, Month.APRIL, 18, 21, 13, 14), user);

    public static final Photo adminAlbum1Photo1 = new Photo(ADMIN_ALBUM1_PHOTO1_ID, "photo 1 admin alb 1 desc",
            LocalDateTime.of(2025, Month.MARCH, 17, 16, 28, 14),
            new File("ph1.jpg", "./picnest/content/photos/100001/100013/ph1.jpg"));

    public static final Album adminAlbum1 = new Album(ADMIN_ALBUM1_ID, "admin album",
            LocalDateTime.of(2025, Month.MARCH, 17, 16, 22, 48), admin);

    public static Album getNewAlbum() {
        return new Album(null, "new album", user);
    }

    public static Album getUpdated() {
        return new Album(USER_ALBUM1_ID, "updated album name", user);
    }

}
