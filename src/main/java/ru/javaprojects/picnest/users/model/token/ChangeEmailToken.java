package ru.javaprojects.picnest.users.model.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.picnest.common.validation.NoHtml;
import ru.javaprojects.picnest.users.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_email_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id", name = "change_email_tokens_unique_user_idx"))
@Getter
@Setter
@NoArgsConstructor
public class ChangeEmailToken extends UserToken {
    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    @Column(name = "new_email", nullable = false)
    private String newEmail;

    public ChangeEmailToken(Long id, String token, LocalDateTime expiryTimestamp, String newEmail, User user) {
        super(id, token, expiryTimestamp, user);
        this.newEmail = newEmail;
    }
}
