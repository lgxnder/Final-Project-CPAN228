package com.humber.sleepPlanRepeat.controllers;
import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.services.EventService;
import com.humber.sleepPlanRepeat.services.UserService;
import com.humber.sleepPlanRepeat.services.GeminiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final GeminiService geminiService;  // Inject GeminiService
    private final EventService eventService;


    // Constructor injection for UserService and GeminiService.
    @Autowired
    public AuthController(UserService userService, GeminiService geminiService, EventService eventService) {
        this.userService = userService;
        this.geminiService = geminiService;
        this.eventService = eventService;
    }

    // Application name injection.
    @Value("sleepPlanRepeat")
    private String applicationName;

    // Registration form.
    @GetMapping("/register")
    public String registerForm(Model model, @RequestParam(required = false) String message) {
        model.addAttribute("user", new User());
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "register";
    }

    // Process registration information from form.
    @PostMapping("/register")
    public String registerSubmit(User user) {

        // Check if username already exists in database.
        int statusCode = userService.saveUser(user);
        if (statusCode == 0) {
            return "redirect:/register?message=Username already taken!";
        }

        return "redirect:/login?message=Registration successful! Please login to continue.";
    }

    // Login.
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

    // POST Login - this will use the Gemini AI functionality to provide the personalized message for the user (upcoming events etc).
    @PostMapping("/login")
    public String loginSubmit(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByUsername(username);
        List<Event> userEvents = eventService.findEventsByUserId(user.getId());
        String personalizedMessage = geminiService.getPersonalizedMessage(username, userEvents);

        // Set the personalized message as a flash attribute.
        redirectAttributes.addFlashAttribute("personalizedMessage", personalizedMessage);

        // Redirect to the calendar page.
        return "redirect:/sleepplanrepeat/calendar/";
    }



    // Logout.
    @GetMapping("/logout")
    public String customLogout() {
        return "redirect:/login?message=logged out successful!";
    }

    // Error.
    @GetMapping("/error")
    public String handleError() {
        return "error";
    }
}
