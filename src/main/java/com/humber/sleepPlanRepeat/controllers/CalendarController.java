package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.CalendarDay;
import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import com.humber.sleepPlanRepeat.services.CalendarService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    private final CalendarService calendarService;

    // Constructor injection
    public CalendarController(
            EventRepository eventRepository,
            UserRepository userRepository,
            CalendarService calendarService
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.calendarService = calendarService;
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

        String formattedMonth = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        model.addAttribute("currentMonth", formattedMonth);

        // Fetch global events.
        List<Event> globalEvents = eventRepository.findByUserIsNull();
        model.addAttribute("globalEvents", globalEvents);

        // Fetch user-specific events if logged in.
        List<Event> userEvents = new ArrayList<>();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userEvents = eventRepository.findByUserId(user.getId());
                model.addAttribute("userEvents", userEvents);
            }
        }

        // Combine all events to return.
        List<Event> allEvents = new ArrayList<>(globalEvents);
        allEvents.addAll(userEvents);

        List<CalendarDay> calendarDays = calendarService.generateCalendarDays(currentMonth, allEvents);
        model.addAttribute("calendarDays", calendarDays);

        return "calendar";
    }

    @GetMapping("/calendar/previous")
    public String getPreviousMonth(@RequestParam(required = false) String month,
                                   @RequestParam(required = false) String year,
                                   Model model, Authentication authentication) {
        YearMonth currentMonth;

        // Parse the month and year if provided
        if (month != null && year != null) {
            currentMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        } else {
            currentMonth = YearMonth.now();
        }

        // Get the previous month
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Redirect to calendar with the new month
        return "redirect:/sleepplanrepeat/calendar?month=" + previousMonth.getMonthValue() + "&year=" + previousMonth.getYear();
    }

    @GetMapping("/calendar/next")
    public String getNextMonth(@RequestParam(required = false) String month,
                               @RequestParam(required = false) String year,
                               Model model, Authentication authentication) {
        YearMonth currentMonth;

        // Parse the month and year if provided
        if (month != null && year != null) {
            currentMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        } else {
            currentMonth = YearMonth.now();
        }

        // Get the next month
        YearMonth nextMonth = currentMonth.plusMonths(1);

        // Redirect to calendar with the new month
        return "redirect:/sleepplanrepeat/calendar?month=" + nextMonth.getMonthValue() + "&year=" + nextMonth.getYear();
    }

    @GetMapping("/calendar/today")
    public String getCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        return "redirect:/sleepplanrepeat/calendar?month=" + currentMonth.getMonthValue() + "&year=" + currentMonth.getYear();
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
                    List<Event> userEvents = eventRepository.findByUserId(user.getId())
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

    @GetMapping("/month")
    public String getMonthView(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model,
            Authentication authentication) {

        // Default to current month and year if not specified.
        YearMonth yearMonth;
        if (year != null && month != null) {
            yearMonth = YearMonth.of(year, month);
        } else {
            yearMonth = YearMonth.now();
        }

        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Get global events for this month.
        List<Event> monthEvents = eventRepository.findGlobalEventsByDateRange(monthStart, monthEnd);

        // Fetch user events for month if authenticated.
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            // User is found in database.
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Get all events for this user and filter by date range.
                List<Event> userMonthEvents = eventRepository.findByUserId(user.getId())
                        .stream()
                        // Convert list of Event objects into Stream.
                        // Like a list, but is open for the client to
                        // act upon whilst more data is still flowing in.
                        // https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html
                        // Check out Stream, Streamable, .stream()

                        .filter(
                                event ->
                                        !event.getStartTime().isBefore(monthStart) &&
                                        !event.getStartTime().isAfter(monthEnd))
                        // Filter elements (like .filter() in React) to ensure
                        // that days saved start and end properly.

                        .collect(Collectors.toList());
                        // Finally, "consume" the Stream and turn it into a list.

                // Add all found user month events to the current list of events.
                monthEvents.addAll(userMonthEvents);
            }
        }

        model.addAttribute("currentYearMonth", yearMonth);
        model.addAttribute("events", monthEvents);

        return "month";
    }
}
