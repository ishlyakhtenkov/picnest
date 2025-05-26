package ru.javaprojects.picnest.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.picnest.common.error.FileException;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.to.FileTo;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@UtilityClass
public class FileUtil {
    private static final String MULTIPART_FILE_MUST_NOT_BE_NULL = "multipartFile must not be null";
    private static final String FILE_BYTES_MUST_NOT_BE_NULL = "fileBytes must not be null";
    private static final String FILE_TO_MUST_NOT_BE_NULL = "fileTo must not be null";
    private static final String FILE_NAME_MUST_NOT_BE_NULL = "fileName must not be null";
    private static final String DIR_PATH_MUST_NOT_BE_NULL = "dirPath must not be null";
    private static final String FILE_PATH_MUST_NOT_BE_NULL = "filePath must not be null";
    private static final String PATH_MUST_NOT_BE_NULL = "path must not be null";

    public static void upload(MultipartFile multipartFile, String dirPath, String fileName) {
        Assert.notNull(multipartFile, MULTIPART_FILE_MUST_NOT_BE_NULL);
        Assert.notNull(dirPath, DIR_PATH_MUST_NOT_BE_NULL);
        Assert.notNull(fileName, FILE_NAME_MUST_NOT_BE_NULL);
        try {
            upload(multipartFile.getBytes(), dirPath, fileName);
        } catch (IOException e) {
            throw new FileException("Failed to upload file: " + fileName +
                    ": " + e.getMessage(), "error.file.failed-to-upload", new Object[]{fileName});
        }
    }

    public static void upload(byte[] fileBytes, String dirPath, String fileName) {
        Assert.notNull(fileBytes, FILE_BYTES_MUST_NOT_BE_NULL);
        Assert.notNull(dirPath, DIR_PATH_MUST_NOT_BE_NULL);
        Assert.notNull(fileName, FILE_NAME_MUST_NOT_BE_NULL);
        if (fileBytes.length == 0) {
            throw new IllegalRequestDataException("File must not be empty: " + fileName, "error.file.must-not-be-empty",
                    new Object[]{fileName});
        }
        try (OutputStream outStream = Files.newOutputStream(Files.createDirectories(Paths.get(dirPath)).resolve(fileName))) {
            outStream.write(fileBytes);
        } catch (IOException e) {
            throw new FileException("Failed to upload file: " + fileName +
                    ": " + e.getMessage(), "error.file.failed-to-upload", new Object[]{fileName});
        }
    }

    public static void upload(FileTo fileTo, String dirPath, String fileName) {
        Assert.notNull(fileTo, FILE_TO_MUST_NOT_BE_NULL);
        Assert.notNull(dirPath, DIR_PATH_MUST_NOT_BE_NULL);
        Assert.notNull(fileName, FILE_NAME_MUST_NOT_BE_NULL);
        if ((fileTo.getInputtedFile() != null && !fileTo.getInputtedFile().isEmpty())) {
            upload(fileTo.getInputtedFile(), dirPath, fileName);
        } else if (fileTo.getInputtedFileBytes() != null && fileTo.getInputtedFileBytes().length != 0) {
            upload(fileTo.getInputtedFileBytes(), dirPath, fileName);
        } else {
            throw new IllegalRequestDataException("File must not be empty: " + fileName, "error.file.must-not-be-empty",
                    new Object[]{fileName});
        }
    }

    public static String normalizePath(String path) {
        Assert.notNull(path, PATH_MUST_NOT_BE_NULL);
        return path.toLowerCase().replace(' ', '_');
    }

    public static void moveFile(String filePath, String dirPath) {
        Assert.notNull(filePath, FILE_PATH_MUST_NOT_BE_NULL);
        Assert.notNull(dirPath, DIR_PATH_MUST_NOT_BE_NULL);
        try {
            Path file = Paths.get(filePath);
            checkNotExistOrNotFile(file);
            Path dir = Paths.get(dirPath);
            if (!file.equals(dir.resolve(file.getFileName()))) {
                Files.createDirectories(dir);
                Files.move(file, dir.resolve(file.getFileName()), REPLACE_EXISTING);
                deleteEmptyParentDirs(file);
            }
        } catch (IOException ex) {
            throw new FileException("Failed to move " + filePath + " to " + dirPath, "error.file.failed-to-move", null);
        }
    }

    public static void deleteFile(String filePath) {
        Assert.notNull(filePath, FILE_PATH_MUST_NOT_BE_NULL);
        try {
            Path file = Paths.get(filePath);
            checkNotExistOrNotFile(file);
            Files.delete(file);
            deleteEmptyParentDirs(file);
        } catch (IOException ex) {
            throw new FileException("Failed to delete file: " + filePath, "error.file.failed-to-delete", new Object[]{filePath});
        }
    }

    public static void deleteDirectory(String dirPath) {
        Assert.notNull(dirPath, DIR_PATH_MUST_NOT_BE_NULL);
        Path dir = Paths.get(dirPath);
        checkNotExistOrNotDirectory(dir);
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw e;
                    }
                }
            });
        } catch (IOException e) {
            throw new FileException("Failed to delete dir: " + dirPath + ": " + e.getMessage(), "error.file.failed-to-delete-dir",
                    new Object[]{dir.getFileName()});
        }
    }

    private static void deleteEmptyParentDirs(Path file) throws IOException {
        Path parentDir = file.getParent();
        while (parentDir != null) {
            try (Stream<Path> otherFiles = Files.list(parentDir)) {
                if (otherFiles.findAny().isEmpty()) {
                    deleteDirectory(parentDir.toString());
                    parentDir = parentDir.getParent();
                } else {
                    parentDir = null;
                }
            }
        }
    }

    private static void checkNotExistOrNotFile(Path file) {
        if (Files.notExists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("File is a directory: " + file);
        }
    }

    private static void checkNotExistOrNotDirectory(Path dir) {
        if (Files.notExists(dir)) {
            throw new IllegalArgumentException("Directory does not exist: " + dir);
        }
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Not a directory: " + dir);
        }
    }
}
