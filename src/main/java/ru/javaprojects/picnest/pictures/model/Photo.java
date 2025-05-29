package ru.javaprojects.picnest.pictures.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import ru.javaprojects.picnest.common.HasId;
import ru.javaprojects.picnest.common.model.BaseEntity;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.common.validation.NoHtml;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Getter
@Setter
@NoArgsConstructor
public class Photo extends BaseEntity implements HasId {

    @Nullable
    @NoHtml
    @Size(max = 1024)
    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created", nullable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime created;

    @NotNull
    @Embedded
    @Valid
    private File file;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long ownerId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    public Photo(Long id, String description, LocalDateTime created, File file, Long ownerId) {
        super(id);
        this.description = description;
        this.created = created;
        this.file = file;
        this.ownerId = ownerId;
    }

    public Photo(Long id, String description, LocalDateTime created, File file, Long ownerId, Album album) {
        this(id, description, created, file, ownerId);
        this.album = album;
    }
}
