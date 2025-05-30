package ru.javaprojects.picnest.pictures;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.javaprojects.picnest.MatcherFactory;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Picture;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static ru.javaprojects.picnest.users.UserTestData.*;

public class PictureTestData {
    public static final MatcherFactory.Matcher<Album> ALBUM_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Album.class, "created", "updated", "owner.password",
                    "owner.registered", "owner.roles");

    public static final MatcherFactory.Matcher<Album> ALBUM_MATCHER_EXCLUDE_PICTURES =
            MatcherFactory.usingIgnoringFieldsComparator(Album.class, "created", "updated", "owner.password",
                    "owner.registered", "owner.roles", "pictures");

    public static final MatcherFactory.Matcher<Picture> PICTURE_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Picture.class, "created");

    public static final String PICTURES_TEST_CONTENT_FILES_PATH = "src/test/test-content-files/pictures";

    public static final long USER_ALBUM1_ID = 100011;
    public static final long USER_ALBUM2_ID = 100012;
    public static final long ADMIN_ALBUM1_ID = 100013;

    public static final String ALBUM_ATTRIBUTE = "album";
    public static final String ALBUMS_ATTRIBUTE = "albums";
    public static final String COUNT_PICTURES_BY_ALBUMS_ATTRIBUTE = "countPicturesByAlbums";
    public static final String LAST_PICTURE_FILE_LINK_BY_ALBUMS_ATTRIBUTE = "lastPictureFileLinkByAlbums";

    public static final long USER_ALBUM1_PHOTO1_ID = 100014;
    public static final long USER_ALBUM1_PHOTO2_ID = 100015;
    public static final long USER_ALBUM1_PHOTO3_ID = 100016;
    public static final long USER_ALBUM2_PHOTO1_ID = 100017;

    public static final long ADMIN_ALBUM1_PHOTO1_ID = 100018;
    public static final long ADMIN_ALBUM1_PHOTO2_ID = 100019;

    public static final Picture USER_ALBUM_1_PICTURE_1 = new Picture(USER_ALBUM1_PHOTO1_ID, "photo 1 user alb 1 desc",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 3),
            new File("ph1.jpg", "./picnest/content/pictures/100000/100011/ph1.jpg"), USER_ID);

    public static final Picture USER_ALBUM_1_PICTURE_2 = new Picture(USER_ALBUM1_PHOTO2_ID, "photo 2 user alb 1 desc",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 7),
            new File("ph2.jpg", "./picnest/content/pictures/100000/100011/ph2.jpg"), USER_ID);

    public static final Picture USER_ALBUM_1_PICTURE_3 = new Picture(USER_ALBUM1_PHOTO3_ID, "photo 3 user alb 1 desc",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 9),
            new File("ph3.jpg", "./picnest/content/pictures/100000/100011/ph3.jpg"), USER_ID);

    public static final Album userAlbum1 = new Album(USER_ALBUM1_ID, "user album 1",
            LocalDateTime.of(2025, Month.MAY, 22, 12, 28, 1), user,
            List.of(USER_ALBUM_1_PICTURE_3, USER_ALBUM_1_PICTURE_2, USER_ALBUM_1_PICTURE_1));

    public static final Picture USER_ALBUM_2_PICTURE_1 = new Picture(USER_ALBUM2_PHOTO1_ID, "photo 1 user alb 2 desc",
            LocalDateTime.of(2025, Month.APRIL, 19, 17, 46, 15),
            new File("ph1.jpg", "./picnest/content/pictures/100000/100012/ph1.jpg"), USER_ID);

    public static final Album userAlbum2 = new Album(USER_ALBUM2_ID, "user album 2",
            LocalDateTime.of(2025, Month.APRIL, 18, 21, 13, 14), user,
            List.of(USER_ALBUM_2_PICTURE_1));

    public static final Picture ADMIN_ALBUM_1_PICTURE_1 = new Picture(ADMIN_ALBUM1_PHOTO1_ID, "photo 1 admin alb 1 desc",
            LocalDateTime.of(2025, Month.MARCH, 17, 16, 28, 14),
            new File("ph1.jpg", "./picnest/content/pictures/100001/100013/ph1.jpg"), ADMIN_ID);

    public static final Picture ADMIN_ALBUM_1_PICTURE_2 = new Picture(ADMIN_ALBUM1_PHOTO2_ID, "photo 2 admin alb 1 desc",
            LocalDateTime.of(2025, Month.MARCH, 17, 16, 34, 59),
            new File("ph2.jpg", "./picnest/content/pictures/100001/100013/ph2.jpg"), ADMIN_ID);

    public static final Album adminAlbum1 = new Album(ADMIN_ALBUM1_ID, "admin album",
            LocalDateTime.of(2025, Month.MARCH, 17, 16, 22, 48), admin,
            List.of(ADMIN_ALBUM_1_PICTURE_2, ADMIN_ALBUM_1_PICTURE_1));

    public static Album getNewAlbum() {
        return new Album(null, "new album", user);
    }

    public static Album getUpdated() {
        return new Album(USER_ALBUM1_ID, "updated album name", user);
    }

    public static final MockMultipartFile NEW_PHOTO_FILE = new MockMultipartFile("file",
            "New photo.jpg",  MediaType.IMAGE_JPEG_VALUE, "new photo file content bytes".getBytes());

    public static final MockMultipartFile DUPLICATE_NAME_NEW_PHOTO_FILE = new MockMultipartFile("file",
            USER_ALBUM_1_PICTURE_1.getFile().getFileName(),  MediaType.IMAGE_JPEG_VALUE, "new photo file content bytes".getBytes());

    public static final MockMultipartFile EMPTY_PHOTO_FILE = new MockMultipartFile("file",
            "New photo.jpg",  MediaType.IMAGE_JPEG_VALUE, new byte[]{});

    public static Picture getNewPhoto() {
        return new Picture(null, null, null,
                new File("New photo.jpg", "./picnest/content/pictures/100000/100011/New photo.jpg"), USER_ID, userAlbum1);
    }

    public static final Map<Long, Integer> userCountPicturesByAlbums =
            Map.of(USER_ALBUM1_ID, userAlbum1.getPictures().size(), USER_ALBUM2_ID, userAlbum2.getPictures().size());

    public static final Map<Long, String> userLastPictureFileLinkByAlbums =
            Map.of(USER_ALBUM1_ID, USER_ALBUM_1_PICTURE_3.getFile().getFileLink(), USER_ALBUM2_ID, USER_ALBUM_2_PICTURE_1.getFile().getFileLink());

}
