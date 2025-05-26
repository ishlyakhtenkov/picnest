package ru.javaprojects.picnest.common.to;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;
import ru.javaprojects.picnest.common.validation.NoHtml;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Predicate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileTo {
    public static Predicate<MultipartFile> IS_IMAGE_FILE =
            inputtedFile -> Objects.requireNonNull(inputtedFile.getContentType()).contains("image/");

    public static Predicate<MultipartFile> IS_YAML_FILE =
            inputtedFile -> Objects.requireNonNull(inputtedFile.getOriginalFilename()).endsWith(".yaml") ||
                    Objects.requireNonNull(inputtedFile.getOriginalFilename()).endsWith(".yml");

    @Nullable
    @NoHtml
    @Size(max = 128)
    private String fileName;

    @Nullable
    @NoHtml
    @Size(max = 512)
    private String fileLink;

    @Nullable
    private MultipartFile inputtedFile;

    @Nullable
    private byte[] inputtedFileBytes;

    public String getSrc() {
        if (fileLink != null) {
            return hasExternalLink() ? fileLink : "/" + fileLink;
        }
        if (inputtedFileBytes == null || fileName == null) {
            return null;
        }
        String srcType = (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) ? "data:application/octet-stream;base64," :
                fileName.endsWith(".svg") ? "data:image/svg+xml;base64," : "data:image/*;base64,";
        return srcType + Base64.getEncoder().encodeToString(inputtedFileBytes);
    }

    public boolean hasExternalLink() {
        return fileLink != null && fileLink.startsWith("https://");
    }

    public boolean isEmpty() {
        return (inputtedFile == null || inputtedFile.isEmpty()) &&
                (inputtedFileBytes == null || inputtedFileBytes.length == 0 || fileName == null || fileName.isBlank());
    }

    public void keepInputtedFile(Predicate<MultipartFile> keepCondition, Runnable notKeptAction) {
        if (inputtedFile != null && !inputtedFile.isEmpty()) {
            if (keepCondition.test(inputtedFile)) {
                keepInputtedFile();
            } else {
                notKeptAction.run();
            }
        }
    }

    private void keepInputtedFile() {
        if (inputtedFile != null) {
            try {
                setInputtedFileBytes(inputtedFile.getBytes());
                setFileName(inputtedFile.getOriginalFilename());
                setInputtedFile(null);
                setFileLink(null);
            } catch (IOException e) {
                throw new IllegalRequestDataException(e.getMessage(), "error.file.failed-to-upload",
                        new Object[]{inputtedFile.getOriginalFilename()});
            }
        } else {
            throw new IllegalRequestDataException("Inputted file must not be null", "error.file.failed-to-upload", null);
        }
    }

    public String getRealFileName() {
        return inputtedFile != null && !inputtedFile.isEmpty() ? inputtedFile.getOriginalFilename() : fileName;
    }
}
