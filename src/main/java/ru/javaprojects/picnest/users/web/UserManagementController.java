package ru.javaprojects.picnest.users.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.picnest.users.model.Role;
import ru.javaprojects.picnest.users.model.User;
import ru.javaprojects.picnest.users.service.UserService;
import ru.javaprojects.picnest.users.to.UserTo;

import static ru.javaprojects.picnest.common.validation.ValidationUtil.checkNew;
import static ru.javaprojects.picnest.common.validation.ValidationUtil.checkNotNew;
import static ru.javaprojects.picnest.users.util.UserUtil.asTo;

@Controller
@RequestMapping(UserManagementController.USERS_URL)
@AllArgsConstructor
@Slf4j
public class UserManagementController {
    static final String USERS_URL = "/management/users";

    private final UserService service;
    private final UniqueEmailValidator emailValidator;
    private final MessageSource messageSource;

    @InitBinder({"user", "userTo"})
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault @SortDefault({"name", "email"}) Pageable p, Model model,
                         RedirectAttributes redirectAttributes) {
        Page<User> users;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/management/users";
            }
            log.info("get users by keyword={} (pageNumber={}, pageSize={})", keyword, p.getPageNumber(), p.getPageSize());
            users = service.getAllByKeyword(keyword.trim(), p);
        } else  {
            log.info("get users (pageNumber={}, pageSize={})", p.getPageNumber(), p.getPageSize());
            users = service.getAll(p);
        }
        if (users.getContent().isEmpty() && users.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/management/users";
        }
        model.addAttribute("users", users);
        model.addAttribute("onlineUsersIds", service.getOnlineUsersIds());
        return "management/users/users";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show user add form");
        model.addAttribute("roles", Role.values());
        model.addAttribute("user", new User());
        return "management/users/user-add-form";
    }

    @PostMapping("/create")
    public String create(@Valid User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "management/users/user-add-form";
        }
        log.info("create {}", user);
        checkNew(user);
        service.create(user);
        redirectAttributes.addFlashAttribute("action", messageSource.getMessage("user.created",
                new Object[]{user.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/management/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for user with id={}", id);
        model.addAttribute("roles", Role.values());
        model.addAttribute("userTo", asTo(service.get(id)));
        return "management/users/user-edit-form";
    }

    @PostMapping("/update")
    public String update(@Valid UserTo userTo, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            if (!userTo.isNew()) {
                model.addAttribute("userName", service.get(userTo.getId()).getName());
            }
            return "management/users/user-edit-form";
        }
        log.info("update {}", userTo);
        checkNotNew(userTo);
        service.update(userTo);
        redirectAttributes.addFlashAttribute("action", messageSource.getMessage("user.updated",
                new Object[]{userTo.getName()}, LocaleContextHolder.getLocale()));
        return "redirect:/management/users";
    }
}
