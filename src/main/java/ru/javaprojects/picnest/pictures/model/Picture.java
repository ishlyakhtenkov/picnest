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
@Table(name = "pictures")
@Getter
@Setter
@NoArgsConstructor
public class Picture extends BaseEntity implements HasId {
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

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

    public Picture(Long id, Type type, String description, LocalDateTime created, File file, Long ownerId) {
        super(id);
        this.type = type;
        this.description = description;
        this.created = created;
        this.file = file;
        this.ownerId = ownerId;
    }

    public Picture(Long id, Type type, String description, LocalDateTime created, File file, Long ownerId, Album album) {
        this(id, type, description, created, file, ownerId);
        this.album = album;
    }
}
