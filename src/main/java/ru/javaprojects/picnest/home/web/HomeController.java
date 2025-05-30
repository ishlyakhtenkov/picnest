package ru.javaprojects.picnest.home.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.pictures.service.PictureService;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@AllArgsConstructor
@Slf4j
public class HomeController {
    private final PictureService service;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.info("Show home page");
        if (AuthUser.safeGet() != null) {
            var albums = service.getAllAlbumsByOwner(AuthUser.authId(), Sort.by(DESC, "created"));
            model.addAttribute("albums", albums);
            model.addAttribute("countPicturesByAlbums", service.countPicturesByAlbums(albums));
        }
        return "home/index";
    }

    @GetMapping("/about")
    public String showAboutPage() {
        log.info("Show about page");
        return "home/about";
    }

    @GetMapping("/contact")
    public String showContactPage() {
        log.info("Show contact page");
        return "home/contact";
    }
}
