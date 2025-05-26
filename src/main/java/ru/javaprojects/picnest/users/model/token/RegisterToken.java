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

import java.time.LocalDateTime;

@Entity
@Table(name = "register_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = "email", name = "register_tokens_unique_email_idx"))
@Getter
@Setter
@NoArgsConstructor
public class RegisterToken extends Token {
    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(min = 5, max = 128)
    @Column(name = "password", nullable = false)
    private String password;

    public RegisterToken(Long id, String token, LocalDateTime expiryTimestamp, String email, String name, String password) {
        super(id, token, expiryTimestamp);
        this.email = email;
        this.name = name;
        this.password = password;
    }
}
