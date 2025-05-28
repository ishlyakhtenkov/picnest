package ru.javaprojects.picnest;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.photos.model.Album;
import ru.javaprojects.picnest.photos.repository.AlbumRepository;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.repository.UserRepository;

@Component
@Profile({"dev", "!test & !default"})
@AllArgsConstructor
public class DevDataGenerator {
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        User admin = userRepository.getExisted(100001);
        for (int i = 0; i < 20; i++) {
            albumRepository.save(new Album(null, "Admin album # " + i, admin));
        }
    }
}
