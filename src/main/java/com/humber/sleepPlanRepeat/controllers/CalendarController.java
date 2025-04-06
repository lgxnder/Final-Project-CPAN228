package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/sleepplanrepeat")
public class CalendarController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Constructor injection
    public CalendarController(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
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
    public String getCalendar(Model model, Authentication authentication) {
        YearMonth currentMonth = YearMonth.now();
        model.addAttribute("currentMonth", currentMonth);

        // Fetch global events.
        List<Event> globalEvents = eventRepository.findByUserIsNull();
        model.addAttribute("globalEvents", globalEvents);

        // Fetch user-specific events if logged in.
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<Event> userEvents = eventRepository.findByUserId((long) user.getId());
                model.addAttribute("userEvents", userEvents);

                // Create combined list of events to return.
                List<Event> allEvents = new ArrayList<>(globalEvents);
                allEvents.addAll(userEvents);
                model.addAttribute("allEvents", allEvents);
            }
        }

         return "calendar";
    }

    @GetMapping("/day")
    public String getDayView(@RequestParam("date") String dateStr, Model model, Authentication authentication) {
        try {

            // Parse date from query param.
            // ex. "2025-03-30"
            // test using http://localhost:8080/sleepplanrepeat/day?date=2025-12-25
            LocalDate selectedDate = LocalDate.parse(dateStr);
            LocalDateTime dayStart = selectedDate.atStartOfDay();
            LocalDateTime dayEnd = selectedDate.atTime(23, 59, 59);

            // Get global events for this day.
            List<Event> globalEvents = eventRepository.findGlobalEventsByDateRange(dayStart, dayEnd);
            List<Event> allEvents = new ArrayList<>(globalEvents);

            // Fetch user events for day if authenticated.
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);

                // User is found in database.
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Get all events for this user and filter by date range.
                    List<Event> userEvents = eventRepository.findByUserId((long) user.getId())
                            .stream()
                            // Convert list of Event objects into Stream.
                            // Like a list, but is open for the client to
                            // act upon whilst more data is still flowing in.
                            // https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html
                            // Check out Stream, Streamable, .stream()

                            .filter(
                                    event ->
                                            !event.getStartTime().isBefore(dayStart) &&
                                            !event.getStartTime().isAfter(dayEnd))
                            // Filter elements (like .filter() in React) to ensure
                            // that days saved start and end properly.

                            .collect(Collectors.toList());
                            // Finally, "consume" the Stream and turn it into a list.

                    // Add all found user events to the current list of events.
                    allEvents.addAll(userEvents);
                    model.addAttribute("userEvents", userEvents);
                }
            }

            model.addAttribute("selectedDate", selectedDate);
            model.addAttribute("events", allEvents);

        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Invalid date format. Please use YYYY-MM-DD.");
        }

        return "day";
    }
}
