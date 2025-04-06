package com.humber.sleepPlanRepeat.controllers;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class  AuthController {

    private final UserService userService;

    // Constructor injection
    private AuthController(UserService userService) {
        this.userService = userService;
    }

    // Application name injection
    @Value("sleepPlanRepeat")
    private String applicationName;

    // Registration form
    @GetMapping("/register")
    public String registerForm(Model model, @RequestParam(required = false) String message) {
        model.addAttribute("user", new User());
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "register";
    }

    // Process registration information from form
    @PostMapping("/register")
    public String registerSubmit(User user) {

        // Check if username already exists in database
        int statusCode = userService.saveUser(user);
        if (statusCode == 0) {
            return "redirect:/register?message=Username already taken!";
        }

        return "redirect:/login?message=Registration successful! Please login to continue.";
    }

    // Login
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String message,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "login";
    }

    // Logout
    @GetMapping("/logout")
    public String customLogout() {
        return "redirect:/login?message=logged out successful!";
    }

    // Error
    @GetMapping("/error")
    public String handleError() {
        return "error";
    }

}

