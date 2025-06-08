package ru.javaprojects.picnest;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.picnest.common.model.File;
import ru.javaprojects.picnest.pictures.model.Album;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.model.Picture;
import ru.javaprojects.picnest.pictures.model.Type;
import ru.javaprojects.picnest.pictures.repository.AlbumRepository;
import ru.javaprojects.picnest.pictures.repository.PictureRepository;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.repository.UserRepository;

import static ru.javaprojects.picnest.pictures.model.Type.IMAGE;

@Component
@Profile({"dev", "!test & !default"})
@AllArgsConstructor
public class DevDataGenerator {
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final PictureRepository pictureRepository;

    @EventListener
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        User admin = userRepository.getExisted(100001);
        for (int i = 0; i < 20; i++) {
            Album created = albumRepository.save(new Album(null, "Admin album # " + i, admin));
            for (int j = 0; j < 10; j++) {
                pictureRepository.save(new Picture(null, IMAGE, null, null, new File("ph1.jpg", "./picnest/content/pictures/100001/100013/ph1.jpg"), admin.getId(), created));
                pictureRepository.save(new Picture(null, IMAGE, null, null, new File("ph2.jpg", "./picnest/content/pictures/100001/100013/ph2.jpg"), admin.getId(), created));
            }
        }
    }
}
