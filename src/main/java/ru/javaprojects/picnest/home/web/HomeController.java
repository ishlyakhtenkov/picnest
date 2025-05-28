package ru.javaprojects.picnest.home.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.photos.service.AlbumService;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@AllArgsConstructor
@Slf4j
public class HomeController {
    private final AlbumService service;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("Show home page");
        if (AuthUser.safeGet() != null) {
            model.addAttribute("albums", service.getAllByOwner(AuthUser.authId(), Sort.by(DESC, "created")));
        }
        return "home/index";
    }
}
