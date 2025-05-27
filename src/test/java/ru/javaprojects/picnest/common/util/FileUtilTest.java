package ru.javaprojects.picnest.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import ru.javaprojects.picnest.ContentFilesManager;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.to.FileTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static ru.javaprojects.picnest.users.UserTestData.USER_ID;

@SpringBootTest
@ActiveProfiles({"dev", "test"})
class FileUtilTest implements ContentFilesManager {
    private static final String NEW_DIR_NAME = "newDir";
    private static final String EXISTING_DIR_NAME = "100012";
    private static final String EXISTING_FILE_NAME = "ph1.jpg";
    private static final String EXISTING_FILE_PATH = EXISTING_DIR_NAME + "/" + EXISTING_FILE_NAME;
    private static final String NOT_EXISTING_DIR_NAME = "notExistDir";
    private static final String NOT_EXISTING_FILE_NAME = "notExistFile.png";
    private static final String FILE_NAME = "fileName";
    private static final byte[] BYTES_ARRAY = new byte[] {1, 2, 3, 4, 5};
    private static final byte[] EMPTY_BYTES_ARRAY = new byte[] {};
    private static final String NOT_EXISTING_FILE_PATH = NOT_EXISTING_DIR_NAME + "/" + NOT_EXISTING_FILE_NAME;

    public static final String USER_PHOTO_ALBUMS_TEST_CONTENT_FILES_PATH = "src/test/test-content-files/photos/" + USER_ID;
    public static final String USER_PHOTO_ALBUMS_FILES_PATH = "./content/photos/" + USER_ID + "/";

    @Override
    public Path getContentPath() {
        return Paths.get(USER_PHOTO_ALBUMS_FILES_PATH);
    }

    @Override
    public Path getContentFilesPath() {
        return Paths.get(USER_PHOTO_ALBUMS_TEST_CONTENT_FILES_PATH);
    }

    @Test
    void uploadMultipartFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(FILE_NAME, BYTES_ARRAY);
        FileUtil.upload(file, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME, file.getName());
        assertEquals(file.getSize(), Files.size(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, file.getName())));
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, file.getName())));
    }

    @Test
    void uploadMultipartFileWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(FILE_NAME, EMPTY_BYTES_ARRAY);
        assertThrows(IllegalRequestDataException.class, () -> FileUtil.upload(file, USER_PHOTO_ALBUMS_FILES_PATH, file.getName()));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, file.getName())));
    }

    @Test
    void uploadMultipartFileWhenFileWithSuchNameExists() throws IOException {
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
        MockMultipartFile file = new MockMultipartFile(EXISTING_FILE_NAME, BYTES_ARRAY);
        FileUtil.upload(file, USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_DIR_NAME, file.getName());
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, file.getName())));
        assertEquals(file.getSize(), Files.size(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, file.getName())));
    }

    @Test
    void uploadBytesArray() throws IOException {
        FileUtil.upload(BYTES_ARRAY, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME, FILE_NAME);
        assertEquals(BYTES_ARRAY.length, Files.size(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, FILE_NAME)));
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, FILE_NAME)));
    }

    @Test
    void uploadBytesArrayWhenArrayEmpty() {
        assertThrows(IllegalRequestDataException.class,
                () -> FileUtil.upload(EMPTY_BYTES_ARRAY, USER_PHOTO_ALBUMS_FILES_PATH, FILE_NAME));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, FILE_NAME)));
    }

    @Test
    void uploadBytesArrayWhenFileWithSuchNameExists() throws IOException {
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
        FileUtil.upload(BYTES_ARRAY, USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_DIR_NAME, EXISTING_FILE_NAME);
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
        assertEquals(BYTES_ARRAY.length, Files.size(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
    }

    @Test
    void uploadFileToWithMultipartFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(FILE_NAME, BYTES_ARRAY);
        FileTo fileTo = new FileTo(null, null, file, null);
        FileUtil.upload(fileTo, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME, file.getName());
        assertEquals(file.getSize(), Files.size(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, file.getName())));
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, file.getName())));
    }

    @Test
    void uploadFileToWithEmptyMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(FILE_NAME, EMPTY_BYTES_ARRAY);
        FileTo fileTo = new FileTo(null, null, file, null);
        assertThrows(IllegalRequestDataException.class, () -> FileUtil.upload(fileTo, USER_PHOTO_ALBUMS_FILES_PATH, file.getName()));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, file.getName())));
    }

    @Test
    void uploadFileToWithFileBytes() throws IOException {
        FileTo fileTo = new FileTo(FILE_NAME, null, null, BYTES_ARRAY);
        FileUtil.upload(fileTo, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME, fileTo.getFileName());
        assertEquals(fileTo.getInputtedFileBytes().length,
                Files.size(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, fileTo.getFileName())));
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, fileTo.getFileName())));
    }

    @Test
    void uploadFileToWithEmptyFileBytes() {
        FileTo fileTo = new FileTo(FILE_NAME, null, null, EMPTY_BYTES_ARRAY);
        assertThrows(IllegalRequestDataException.class,
                () -> FileUtil.upload(fileTo, USER_PHOTO_ALBUMS_FILES_PATH, fileTo.getFileName()));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, fileTo.getFileName())));
    }

    @Test
    void uploadFileToWithoutMultipartFileAndFileBytes() {
        FileTo fileTo = new FileTo(FILE_NAME, null, null, null);
        assertThrows(IllegalRequestDataException.class,
                () -> FileUtil.upload(fileTo, USER_PHOTO_ALBUMS_FILES_PATH, fileTo.getFileName()));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, fileTo.getFileName())));
    }

    @Test
    void deleteDirectory() {
        FileUtil.deleteDirectory(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_DIR_NAME);
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME)));
    }

    @Test
    void deleteDirectoryWhenNotExists() {
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH + NOT_EXISTING_DIR_NAME)));
        assertThrows(IllegalArgumentException.class,
                () -> FileUtil.deleteDirectory(USER_PHOTO_ALBUMS_FILES_PATH + NOT_EXISTING_DIR_NAME));
    }

    @Test
    void deleteDirectoryWhenNotDirectory() {
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH)));
        assertThrows(IllegalArgumentException.class,
                () -> FileUtil.deleteDirectory(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH));
    }

    @Test
    void moveFile() {
        FileUtil.moveFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME);
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_FILE_PATH)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME)));
    }

    @Test
    void moveFileToSameDir() {
        FileUtil.moveFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH, USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_DIR_NAME);
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, EXISTING_FILE_NAME)));
    }

    @Test
    void moveFileWhenFileNotExists() {
        assertThrows(IllegalArgumentException.class, () ->
                FileUtil.moveFile(USER_PHOTO_ALBUMS_FILES_PATH + NOT_EXISTING_FILE_PATH, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, NOT_EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME)));
    }

    @Test
    void moveFileWhenFileNotFile() {
        assertThrows(IllegalArgumentException.class, () ->
                FileUtil.moveFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_DIR_NAME, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME)));
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME)));
    }

    @Test
    void moveFileWhenFileExistsInNewDir() throws IOException {
        Files.createDirectories(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME));
        Files.createFile(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, EXISTING_FILE_NAME));

        FileUtil.moveFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME);
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_FILE_PATH)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME)));
    }

    @Test
    void moveFileWhenFileNotAloneInOldDir() throws IOException {
        String anotherFileName = "anotherFile.svg";
        Files.createFile(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, anotherFileName));

        FileUtil.moveFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH, USER_PHOTO_ALBUMS_FILES_PATH + NEW_DIR_NAME);
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, NEW_DIR_NAME, EXISTING_FILE_NAME)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_FILE_PATH)));
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, anotherFileName)));
    }

    @Test
    void deleteFile() {
        FileUtil.deleteFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH);
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_FILE_PATH)));
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME)));
    }

    @Test
    void deleteFileWhenNotExists() {
        assertThrows(IllegalArgumentException.class,
                () -> FileUtil.deleteFile(USER_PHOTO_ALBUMS_FILES_PATH + NOT_EXISTING_FILE_PATH));
    }

    @Test
    void deleteFileWhenNotFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUtil.deleteFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_DIR_NAME));
    }

    @Test
    void deleteFileWhenNotAloneInDir() throws IOException {
        String anotherFileName = "anotherFile.svg";
        Files.createFile(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, anotherFileName));

        FileUtil.deleteFile(USER_PHOTO_ALBUMS_FILES_PATH + EXISTING_FILE_PATH);
        assertTrue(Files.notExists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_FILE_PATH)));
        assertTrue(Files.exists(Paths.get(USER_PHOTO_ALBUMS_FILES_PATH, EXISTING_DIR_NAME, anotherFileName)));
    }

    @Test
    void normalizeFileName() {
        assertEquals("apache_tomcat.svg", FileUtil.normalizePath("Apache Tomcat.svg"));
        assertEquals("spring.svg", FileUtil.normalizePath("Spring.svg"));
        assertEquals("apache__tomcat_file.svg", FileUtil.normalizePath("Apache  Tomcat File.svg"));
        assertEquals("apache_tomcat_file.svg", FileUtil.normalizePath("Apache_Tomcat_File.svg"));
    }

    @Test
    void isFileToEmptyWhenFileToHasMultipartFile() {
        FileTo fileTo = new FileTo(null, null, new MockMultipartFile(FILE_NAME, BYTES_ARRAY), null);
        assertFalse(fileTo.isEmpty());
    }

    @Test
    void isFileToEmptyWhenFileToHasFileBytes() {
        FileTo fileTo = new FileTo(FILE_NAME, null, null, BYTES_ARRAY);
        assertFalse(fileTo.isEmpty());
    }

    @Test
    void isFileToEmptyWhenFileToHasEmptyMultipartFile() {
        FileTo fileTo = new FileTo(null, null, new MockMultipartFile(FILE_NAME, EMPTY_BYTES_ARRAY), null);
        assertTrue(fileTo.isEmpty());
    }

    @Test
    void isFileToEmptyWhenFileToHasEmptyFileBytes() {
        FileTo fileTo = new FileTo(FILE_NAME, null, null, EMPTY_BYTES_ARRAY);
        assertTrue(fileTo.isEmpty());
    }

    @Test
    void isFileToEmptyWhenFileToToHasFileBytesAndHasNoFileName() {
        FileTo fileTo = new FileTo(null, null, null, BYTES_ARRAY);
        assertTrue(fileTo.isEmpty());
    }

    @Test
    void isFileToEmptyWhenFileToToHasFileBytesAndHasEmptyFileName() {
        FileTo fileTo = new FileTo("", null, null, BYTES_ARRAY);
        assertTrue(fileTo.isEmpty());
    }
}
