package ru.javaprojects.picnest.photos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.javaprojects.picnest.common.HasIdAndName;
import ru.javaprojects.picnest.common.model.BaseEntity;
import ru.javaprojects.picnest.common.validation.NoHtml;
import ru.javaprojects.picnest.users.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "albums",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}, name = "albums_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Album extends BaseEntity implements HasIdAndName {
    @NotBlank
    @NoHtml
    @Size(max = 256)
    @Column(name = "name", nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created", nullable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated", columnDefinition = "timestamp")
    private LocalDateTime updated;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    public Album(Long id, String name, User owner) {
        super(id);
        this.name = name;
        this.owner = owner;
    }

    public Album(Long id, String name, LocalDateTime created, User owner) {
        this(id, name, owner);
        this.created = created;
    }

    @Override
    public String toString() {
        return String.format("Album[id=%d, name=%s]", id, name);
    }
}
