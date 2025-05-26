package ru.javaprojects.picnest.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.repository.BaseRepository;
import ru.javaprojects.picnest.users.model.User;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findAll(Pageable pageable);

    @Query("""
            SELECT u FROM User u WHERE UPPER(u.name) LIKE UPPER(CONCAT('%', :keyword, '%')) OR
            UPPER(u.email) LIKE UPPER(CONCAT('%', :keyword, '%'))""")
    Page<User> findAllByKeyword(String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.enabled = TRUE AND UPPER(u.name) LIKE UPPER(CONCAT('%', :keyword, '%'))")
    Page<User> findAllEnabledByKeyword(String keyword, Pageable pageable);
}
