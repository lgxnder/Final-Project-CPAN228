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
import java.time.format.DateTimeParseException;
import java.util.List;

//CalendarController work in progress
@Controller
@RequestMapping("/sleepplanrepeat")
public class CalendarController {

    private final EventRepository eventRepository;
    public CalendarController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Value("sleepPlanRepeat")
    private String applicationName;

    @GetMapping("/")
    public String redirectToLanding() {
        return "redirect:/sleepplanrepeat/landing";
    }

    @GetMapping("/landing")
    public String getLanding() {
        return "landing";
    }

    @GetMapping("/calendar")
    public String getCalendar(Model model) {
        YearMonth currentMonth = YearMonth.now(); // Should be March 2025 as of today.
        model.addAttribute("currentMonth", currentMonth);

        // Fetch global events.
        List<Event> globalEvents = eventRepository.findByUserIsNull();
        model.addAttribute("globalEvents", globalEvents);

        // TODO: Fetch user-specific events if logged in
         return "calendar";
    }

    @GetMapping("/day")
    public String getDayView(@RequestParam("date") String dateStr, Model model) {
        try {

            // Parse date from query param.
            // ex. "2025-03-30"
            // test using http://localhost:8080/sleepplanrepeat/day?date=2025-12-25
            LocalDate selectedDate = LocalDate.parse(dateStr);
            LocalDateTime dayStart = selectedDate.atStartOfDay();
            LocalDateTime dayEnd = selectedDate.atTime(23, 59, 59);

            List<Event> globalEvents = eventRepository.findGlobalEventsByDateRange(dayStart, dayEnd);

            /*
            // Fetch user events for day.
            List<Event> userEvents = List.of();
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                // make userrepository.
                User user = userRepo.find by username(username)
                userEvents = eventRepository.findByUserId(user.getId())

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
             }
             */

            // TODO: fetch user specific events if authenticated
            // List<Event> allEvents = new ArrayList<>();
            // allEvents.addAll(globalEvents);
            // allEvents.addAll(userEvents);

            model.addAttribute("selectedDate", selectedDate);
            model.addAttribute("events", globalEvents);
            // replace globalEvents with allEvents here once userEvents is implemented.
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Invalid date format. Please use YYYY-MM-DD.");
        }
        return "day";
    }
}
