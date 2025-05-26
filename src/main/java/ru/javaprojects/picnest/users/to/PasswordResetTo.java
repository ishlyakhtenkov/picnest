package ru.javaprojects.picnest.users.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetTo {
    @NotBlank
    @Size(min = 5, max = 32)
    private String password;

    @NotBlank
    private String token;
}

