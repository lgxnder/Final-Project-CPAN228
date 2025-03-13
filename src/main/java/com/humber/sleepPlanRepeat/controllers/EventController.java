package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/sleepplanrepeat")
public class EventController {
    // See guidance for this class in ~/docs/schedule_overview.txt .

    // Utilize constructor injection.
    private final EventRepository eventRepository;
    private final EventService eventService;

    public EventController(
            EventRepository eventRepository,
            EventService eventService)
    {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    // Initialize values for use in Thymeleaf templating.
    @Value("sleepPlanRepeat")
    private String applicationName;

    // Overview endpoint. USER is redirected here after log-in.
    // USER SHOULD REDIRECT TO CURRENT MONTH OF CURRENT YEAR
    // /overview/{year}/{month}
    @GetMapping("/overview")
    public String overview(Model model) {
        model.addAttribute("appName", applicationName);
        return "overview";
    }
}
