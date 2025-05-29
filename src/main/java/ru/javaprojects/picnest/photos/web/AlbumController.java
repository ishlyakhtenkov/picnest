package ru.javaprojects.picnest.photos.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.photos.service.AlbumService;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@RequestMapping(value = AlbumController.ALBUMS_URL)
@AllArgsConstructor
@Slf4j
public class AlbumController {
    static final String ALBUMS_URL = "/albums";

    private final AlbumService service;

    @GetMapping("/{id}")
    public String showAlbum(@PathVariable long id, Model model) {
        log.info("show album with id={}", id);
        model.addAttribute("album", service.get(id, AuthUser.authId(), Sort.by(DESC, "photos.created")));
        return "photos/album";
    }
}
