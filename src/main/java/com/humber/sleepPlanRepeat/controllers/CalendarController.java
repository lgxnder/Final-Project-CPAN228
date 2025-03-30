package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/sleepplanrepeat")
public class CalendarController {
    // Inject EventRepository here.

    // Initialize values for use in Thymeleaf templating.
    @Value("sleepPlanRepeat")
    private String applicationName;


    // get mapping /overview
    @GetMapping("/overview")
    // public String getOverview(Model model)
    // YearMonth currentMonth = YearMonth.now() // should be March 2025 as of today.
    // model.addAttribute("currentMonth", currentMonth);

    // Fetch global events
    // List<Event> globalEvents = eventRepository.findByUserIsNull();
    // model.addAttribute("globalEvents", globalEvents);

    // TODO: Fetch user-specific events if logged in
    return "overview";
}
