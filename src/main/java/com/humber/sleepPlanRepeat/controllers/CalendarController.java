package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;


@Controller
@RequestMapping("/sleepplanrepeat")
public class CalendarController {
    // Inject EventRepository here.
    private final EventRepository eventRepository;

    //Constructor for eventRepository
    public CalendarController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Initialize values for use in Thymeleaf templating.
    @Value("sleepPlanRepeat")
    private String applicationName;

    // get mapping /overview
    @GetMapping("/overview")
    // public String getOverview(Model model)
    public String getOverview(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        // YearMonth currentMonth = YearMonth.now() // should be March 2025 as of today.
        YearMonth currentYearMonth = YearMonth.now();
        // model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYearMonth", currentYearMonth);
        return "overview";
    }

    // Fetch global events
    @GetMapping("/events")
    public String getEvents(Model model) {
        // List<Event> globalEvents = eventRepository.findByUserIsNull();
        List<Event> globalEvents = eventRepository.findByUserIsNull();
        model.addAttribute("events", eventRepository.findAll());
        // model.addAttribute("globalEvents", globalEvents);
        model.addAttribute("globalEvents", globalEvents);
        return "events";
    }

    // TODO: Fetch user-specific events if logged in
    //Update day(By the number of the day)
    @PutMapping("/day/{num}")
    public String setDay(@PathVariable int num, Model model) {

        return "day";
    }

}
