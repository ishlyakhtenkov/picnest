package ru.javaprojects.picnest.users.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javaprojects.picnest.app.AuthUser;

@Controller
@RequestMapping(LoginController.LOGIN_URL)
@AllArgsConstructor
@Slf4j
public class LoginController {
    public static final String LOGIN_URL = "/login";

    private final MessageSource messageSource;

    @GetMapping
    public String showLoginPage(@RequestParam(name = "error", required = false) String error, Model model) {
        log.info("show login page");
        if (AuthUser.safeGet() == null) {
            if (error != null) {
                model.addAttribute("error", messageSource.getMessage(error, null,
                        "Bad credentials", LocaleContextHolder.getLocale()));
            }
            return "profile/login";
        }
        return "redirect:/";
    }
}
