package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showUserProfile(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            userOptional.ifPresent(user -> model.addAttribute("user", user));
            return "profile";
        }
        return "redirect:/login";
    }

    @PostMapping("/update")
    public String updateUserProfile(User updatedUser,
                                    @RequestParam(value = "newPassword", required = false) String newPassword,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> existingUserOptional = userRepository.findByUsername(username);

            existingUserOptional.ifPresent(existingUser -> {
                boolean updated = false;

                // Update username
                if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
                    if (userRepository.findByUsername(updatedUser.getUsername()).isEmpty()) {
                        existingUser.setUsername(updatedUser.getUsername());
                        updated = true;
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Username already exists.");
                        return;
                    }
                }

                // Update email
                existingUser.setEmail(updatedUser.getEmail());
                updated = true;

                // Update email notifications
                existingUser.setEnableEmailNotifications(updatedUser.isEnableEmailNotifications());
                updated = true;

                // Update password if a new one is provided
                if (newPassword != null && !newPassword.isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(newPassword));
                    updated = true;
                    redirectAttributes.addFlashAttribute("message", "Profile and password updated successfully!");
                } else if (updated) {
                    redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
                }

                if (updated) {
                    userRepository.save(existingUser);
                }
            });
            return "redirect:/sleepplanrepeat/profile";
        }
        return "redirect:/login";
    }
}