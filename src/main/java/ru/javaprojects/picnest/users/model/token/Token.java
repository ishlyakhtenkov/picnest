package ru.javaprojects.picnest.users.model.token;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.picnest.common.model.BaseEntity;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Token extends BaseEntity {
    @NotBlank
    @Column(name = "token", nullable = false)
    protected String token;

    @NotNull
    @Column(name = "expiry_timestamp", nullable = false)
    protected LocalDateTime expiryTimestamp;

    public Token(Long id, String token, LocalDateTime expiryTimestamp) {
        super(id);
        this.token = token;
        this.expiryTimestamp = expiryTimestamp;
    }

    @Override
    public String toString() {
        return String.format("%s[id=%d, expiryTimestamp=%s]", getClass().getSimpleName(),id, expiryTimestamp);
    }
}
