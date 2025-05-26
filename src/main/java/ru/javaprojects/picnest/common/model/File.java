package ru.javaprojects.picnest.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.picnest.common.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class File {
    @NotBlank
    @NoHtml
    @Size(max = 128)
    @Column(name = "file_name")
    private String fileName;

    @NotBlank
    @NoHtml
    @Size(max = 512)
    @Column(name = "file_link")
    private String fileLink;

    public String getSrc() {
        return fileLink != null ? (hasExternalLink() ? fileLink : "/" + fileLink) : null;
    }

    public boolean hasExternalLink() {
        return fileLink != null && fileLink.startsWith("https://");
    }
}
