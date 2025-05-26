package ru.javaprojects.picnest.users.web;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.picnest.app.AuthUser;
import ru.javaprojects.picnest.users.service.PasswordResetService;
import ru.javaprojects.picnest.users.service.UserService;
import ru.javaprojects.picnest.users.to.ProfileTo;

@RestController
@RequestMapping(value = ProfileController.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProfileRestController {
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@RequestParam String email) {
        log.info("forgot password for user with email={}", email);
        passwordResetService.sendPasswordResetEmail(email);
    }

    @PatchMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestParam @Size(min = 5, max = 32, message = "{validation.password.Size}") String password) {
        log.info("change password for user with id={}", AuthUser.authId());
        userService.changePassword(AuthUser.authId(), password);
    }

    @GetMapping("/by-keyword")
    public Page<ProfileTo> getProfilesByKeyword(@RequestParam String keyword,
                                                @PageableDefault @SortDefault("name") Pageable p) {
        log.info("get profiles by keyword={} (pageNumber={}, pageSize={})", keyword, p.getPageNumber(), p.getPageSize());
        return userService.getAllEnabledProfilesByKeyword(keyword, p);
    }
}
