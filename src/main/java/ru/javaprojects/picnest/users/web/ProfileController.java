package ru.javaprojects.picnest.users.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.common.to.FileTo;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.service.ChangeEmailService;
import ru.javaprojects.picnest.users.service.PasswordResetService;
import ru.javaprojects.picnest.users.service.UserService;
import ru.javaprojects.picnest.users.to.PasswordResetTo;
import ru.javaprojects.picnest.users.to.ProfileTo;

import static ru.javaprojects.picnest.users.util.UserUtil.asProfileTo;

@Controller
@RequestMapping(ProfileController.PROFILE_URL)
@AllArgsConstructor
@Slf4j
public class ProfileController {
    public static final String PROFILE_URL = "/profile";

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final ChangeEmailService changeEmailService;
    private final UniqueEmailValidator emailValidator;
    private final MessageSource messageSource;

    @InitBinder("profileTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        log.info("show reset password form by token={}", token);
        passwordResetService.checkToken(token);
        model.addAttribute("passwordResetTo",  new PasswordResetTo(null, token));
        return "profile/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid PasswordResetTo passwordResetTo, BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profile/reset-password";
        }
        log.info("reset password by token={}", passwordResetTo.getToken());
        passwordResetService.resetPassword(passwordResetTo);
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage("reset-password.success", null, LocaleContextHolder.getLocale()));
        return "redirect:/login";
    }

    @GetMapping("/{id}/view")
    public String showProfile(@PathVariable long id, Model model) {
        log.info("show profile with id={}", id);
        model.addAttribute("user", userService.get(id));
        model.addAttribute("rating", userService.getUserRating(id));
        return "profile/profile";
    }

    @GetMapping("/edit")
    public String showEditForm(Model model) {
        log.info("show profile edit form for user with id={}", AuthUser.authId());
        model.addAttribute("profileTo", asProfileTo(userService.get(AuthUser.authId())));
        return "profile/profile-edit-form";
    }

    @PostMapping
    public String update(@Valid ProfileTo profileTo, BindingResult result, RedirectAttributes redirectAttributes) {
        profileTo.setId(AuthUser.authId());
        if (result.hasErrors()) {
            if (profileTo.getAvatar() != null) {
                profileTo.getAvatar().keepInputtedFile(FileTo.IS_IMAGE_FILE, () -> profileTo.setAvatar(null));
            }
            return "profile/profile-edit-form";
        }
        log.info("update {}", profileTo);
        User user = userService.update(profileTo);
        AuthUser.get().getUser().setName(user.getName());
        AuthUser.get().getUser().setAvatar(user.getAvatar());
        String messageCode = profileTo.getEmail().equalsIgnoreCase(user.getEmail()) ? "profile.updated" :
                "profile.updated.confirm-email";
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage(messageCode,  null, LocaleContextHolder.getLocale()));
        return "redirect:/profile/" + AuthUser.authId() + "/view";
    }

    @GetMapping("/change-email/confirm")
    public String confirmChangeEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        log.info("confirm change email for user with id={} by token={}", AuthUser.authId(), token);
        changeEmailService.confirmChangeEmail(token, AuthUser.authId());
        redirectAttributes.addFlashAttribute("action",
                messageSource.getMessage("change-email.email-confirmed", null, LocaleContextHolder.getLocale()));
        return "redirect:/profile/" + AuthUser.authId() + "/view";
    }
}
