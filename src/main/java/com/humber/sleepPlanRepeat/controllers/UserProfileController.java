package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/sleepplanrepeat/profile")
public class UserProfileController {

    private final UserRepository userRepository;

    @Autowired
    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showUserProfile(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            userOptional.ifPresent(user -> model.addAttribute("user", user));
            return "profile";
        }
        return "redirect:/login"; // Redirect to login if not authenticated
    }

    @PostMapping("/update-notifications")
    public String updateEmailNotifications(@RequestParam("enableEmailNotifications") boolean enableEmailNotifications,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            userOptional.ifPresent(user -> {
                user.setEnableEmailNotifications(enableEmailNotifications);
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("message", "Notification settings updated successfully!");
            });
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to update settings.");
        }
        return "redirect:/sleepplanrepeat/profile"; // Redirect back to the profile page
    }
}