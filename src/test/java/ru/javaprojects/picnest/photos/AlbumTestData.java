package ru.javaprojects.picnest.photos;

import ru.javaprojects.picnest.MatcherFactory;
import ru.javaprojects.picnest.photos.model.Album;

import java.time.LocalDateTime;
import java.time.Month;

import static ru.javaprojects.picnest.users.UserTestData.admin;
import static ru.javaprojects.picnest.users.UserTestData.user;

public class AlbumTestData {
    public static final MatcherFactory.Matcher<Album> ALBUM_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Album.class, "created", "updated", "owner.password",
                    "owner.registered", "owner.roles");

    public static final String ALBUMS_TEST_CONTENT_FILES_PATH = "src/test/test-content-files/photos";

    public static final long USER_ALBUM1_ID = 100011;
    public static final long USER_ALBUM2_ID = 100012;
    public static final long ADMIN_ALBUM1_ID = 100013;

    public static final Album userAlbum1 = new Album(USER_ALBUM1_ID, "user album 1",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 1), user);

    public static final Album userAlbum2 = new Album(USER_ALBUM2_ID, "user album 2",
            LocalDateTime.of(2025, Month.APRIL, 18, 21, 13, 14), user);

    public static final Album adminAlbum1 = new Album(ADMIN_ALBUM1_ID, "admin album",
            LocalDateTime.of(2025, Month.MARCH, 17, 16, 22, 48), admin);

    public static Album getNewAlbum() {
        return new Album(null, "new album", user);
    }

    public static Album getUpdated() {
        return new Album(USER_ALBUM1_ID, "updated album name", user);
    }

}
