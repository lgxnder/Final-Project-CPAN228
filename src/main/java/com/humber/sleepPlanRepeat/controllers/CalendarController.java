package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/sleepplanrepeat")
public class CalendarController {

    private final EventRepository eventRepository;

    public CalendarController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Value("sleepPlanRepeat")
    private String applicationName;

    @GetMapping("/overview")
    public String getOverview(Model model) {
        YearMonth currentMonth = YearMonth.now(); // Should be March 2025 as of today.
        model.addAttribute("currentMonth", currentMonth);

        // Fetch global events.
        List<Event> globalEvents = eventRepository.findByUserIsNull();
        model.addAttribute("globalEvents", globalEvents);

        // TODO: Fetch user-specific events if logged in
        return "overview";
    }

    @GetMapping("/day")
    public String getDayView(@RequestParam("date") String dateStr, Model model) {
        // Parse date from query param.
        // ex. "2025-03-30"
        LocalDate selectedDate = LocalDate.parse(dateStr);

        LocalDateTime dayStart = selectedDate.atStartOfDay();
        LocalDateTime dayEnd = selectedDate.atTime(23, 59, 59);

        // Fetch global events for day.
        List<Event> globalEvents = eventRepository.findByUserIsNull()
                // Based on result from repo method -- findByUserIsNull,
                // perform the following methods:

                .stream()
                // Convert list of Event objects into Stream.
                // Like a list, but is open for the client to
                // act upon whilst more data is still flowing in.
                // https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html
                // Check out Stream, Streamable, .stream()

                .filter(event -> !event.getStartTime().isBefore(dayStart) && !event.getStartTime().isAfter(dayEnd))
                // Filter elements (like .filter() in React) to ensure
                // that days saved start and end properly.

                .collect(Collectors.toList());
                // Finally, "consume" the Stream and turn it into a list.

        // TODO: fetch user specific events if authenticated
        // right now, we are using global events:
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("events", globalEvents);

        return "day";
    }
}
