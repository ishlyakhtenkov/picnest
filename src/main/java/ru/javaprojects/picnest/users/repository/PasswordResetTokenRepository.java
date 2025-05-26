package ru.javaprojects.picnest.users.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.users.model.token.PasswordResetToken;

import java.util.Optional;

@Transactional(readOnly = true)
public interface PasswordResetTokenRepository extends TokenRepository<PasswordResetToken> {

    @Query("SELECT p FROM PasswordResetToken p LEFT JOIN FETCH p.user WHERE UPPER(p.user.email) = UPPER(:email)")
    Optional<PasswordResetToken> findByUserEmailIgnoreCase(String email);

    @Query("SELECT p FROM PasswordResetToken p LEFT JOIN FETCH p.user WHERE p.token =:token")
    Optional<PasswordResetToken> findByToken(String token);
}

