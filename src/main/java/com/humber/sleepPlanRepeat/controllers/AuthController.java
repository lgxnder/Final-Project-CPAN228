package com.humber.sleepPlanRepeat.controllers;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//AuthController work in progress
public class AuthController {

    private final UserService userService;

    //Constructor of authcontroller
    private AuthController(UserService userService) {
        this.userService = userService;
    }

    //Injecting application name
    @Value("sleepPlanRepeat")
    private String applicationName;

    //error endpoint
    @GetMapping("/error")
    public String handleError() {
        return "/error";
    }

    //login endpoint
    @GetMapping("/login")
    public String login(Model model,@RequestParam(required = false) String error,
                        @RequestParam(required = false) String message) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "/login";
    }

    //custom logout endpoint
    @GetMapping("/logout")
    public String customLogout() {
        return "redirect:/login?message=logged out successful!";
    }

    //custom registration endpoint
    @GetMapping("/register")
    public String register(Model model, @RequestParam(required = false) String message) {
        model.addAttribute("message", message);
        return "/register";
    }

    //custom registration for saving user
    @PostMapping("/register")
    public String register(User user){
        int statusCode = userService.saveUser(user);
        //if statement for user exists or not
        if(statusCode == 0){
            return "redirect:/register.html?message=Username already taken!";
        }
        return "redirect:/login?message=Registration successful! Please login to continue.";
    }
}

