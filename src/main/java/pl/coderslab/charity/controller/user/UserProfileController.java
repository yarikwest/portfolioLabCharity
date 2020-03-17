package pl.coderslab.charity.controller.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.charity.dto.UpdatePasswordDto;
import pl.coderslab.charity.dto.UserDto;
import pl.coderslab.charity.dto.UserMapper;
import pl.coderslab.charity.model.User;
import pl.coderslab.charity.service.UserService;

import javax.validation.Valid;

@Controller
@RequestMapping("user/profile")
class UserProfileController {
    private final UserMapper userMapper;
    private final UserService userService;

    UserProfileController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @GetMapping
    public String panel(Model model) {
        User loggedInUser = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        model.addAttribute("updatePassword", new UpdatePasswordDto());
        model.addAttribute("user", userMapper.userToUserDto(loggedInUser));
        return "user/profile";
    }

    @PostMapping("update-password")
    public String updatePassword(@Valid @ModelAttribute("updatePassword") UpdatePasswordDto updatePassword, BindingResult bindingResult, Model model) {

        User loggedInUser = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!userService.checkIfValidOldPassword(loggedInUser, updatePassword.getOldPassword())) {
            bindingResult.rejectValue("oldPassword", "message.invalidOldPassword");
            model.addAttribute("user", userMapper.userToUserDto(loggedInUser));
            return "user/profile";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userMapper.userToUserDto(loggedInUser));
            return "user/profile";
        }

        userService.changeUserPassword(loggedInUser, updatePassword.getPassword());

        return "user/profile";
    }

    @PostMapping("update-email")
    public String updateEmail(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("updatePassword", new UpdatePasswordDto());
            return "user/profile";
        }

        User loggedInUser = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        userService.changeUserEmail(loggedInUser, userDto.getEmail());

        return "redirect:/logout";
    }
}
