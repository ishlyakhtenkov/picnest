package ru.javaprojects.picnest.users.to;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.picnest.common.HasEmailAndPassword;
import ru.javaprojects.picnest.common.HasIdAndEmail;
import ru.javaprojects.picnest.common.to.BaseTo;
import ru.javaprojects.picnest.common.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class RegisterTo extends BaseTo implements HasIdAndEmail, HasEmailAndPassword {
    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    private String email;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String name;

    @NotBlank
    @Size(min = 5, max = 32)
    private String password;

    public RegisterTo(Long id, String email, String name, String password) {
        super(id);
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("RegisterTo[id=%d, email=%s]", id, email);
    }
}
