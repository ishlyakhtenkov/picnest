package ru.javaprojects.picnest.users.web;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.picnest.common.HasIdAndEmail;
import ru.javaprojects.picnest.users.repository.UserRepository;

import java.util.Objects;

@Component
@AllArgsConstructor
public class UniqueEmailValidator implements org.springframework.validation.Validator {
    public static final String DUPLICATE_ERROR_CODE = "Duplicate";

    private final UserRepository repository;
    private final MessageSource messageSource;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return HasIdAndEmail.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        HasIdAndEmail user = ((HasIdAndEmail) target);
        if (StringUtils.hasText(user.getEmail())) {
            repository.findByEmailIgnoreCase(user.getEmail())
                    .ifPresent(dbUser -> {
                        if (user.isNew() || !Objects.equals(user.getId(), dbUser.getId())) {
                            errors.rejectValue("email", DUPLICATE_ERROR_CODE,
                                    messageSource.getMessage("error.duplicate.email", null,
                                            LocaleContextHolder.getLocale()));
                        }
                    });
        }
    }
}

