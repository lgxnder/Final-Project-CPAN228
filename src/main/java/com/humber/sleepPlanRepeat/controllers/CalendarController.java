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

    private final EventRepository eventRepository;

    public CalendarController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Value("sleepPlanRepeat")
    private String applicationName;

    // get mapping /overview
    @GetMapping("/overview")
    public String getOverview(Model model) {
        YearMonth currentMonth = YearMonth.now(); // should be March 2025 as of today.
        model.addAttribute("currentMonth", currentMonth);

        // Fetch global events
        List<Event> globalEvents = eventRepository.findByUserIsNull();
        model.addAttribute("globalEvents", globalEvents);

        // TODO: Fetch user-specific events if logged in
        return "overview";
    }
}
