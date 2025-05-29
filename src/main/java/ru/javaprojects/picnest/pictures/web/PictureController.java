package ru.javaprojects.picnest.pictures.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.pictures.service.PictureService;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@RequestMapping(value = PictureController.ALBUMS_URL)
@AllArgsConstructor
@Slf4j
public class PictureController {
    static final String ALBUMS_URL = "/albums";

    private final PictureService service;

    @GetMapping("/{id}")
    public String showAlbum(@PathVariable long id, Model model) {
        log.info("show album with id={}", id);
        model.addAttribute("album", service.getAlbum(id, AuthUser.authId(), Sort.by(DESC, "photos.created")));
        return "photos/album";
    }
}
