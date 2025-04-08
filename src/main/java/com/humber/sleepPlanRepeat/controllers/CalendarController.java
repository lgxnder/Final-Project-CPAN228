package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.CalendarDay;
import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import com.humber.sleepPlanRepeat.services.CalendarService;
import com.humber.sleepPlanRepeat.services.UserService;
import com.humber.sleepPlanRepeat.services.GeminiService;
import com.humber.sleepPlanRepeat.services.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;
    private final EventService eventService;
    private final GeminiService geminiService;

    // Constructor injection
    public CalendarController(
            EventRepository eventRepository,
            UserRepository userRepository,
            CalendarService calendarService,
            UserService userService,
            EventService eventService,
            GeminiService geminiService
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.calendarService = calendarService;
        this.userService = userService;
        this.eventService = eventService;
        this.geminiService = geminiService;
    }

    @Value("sleepPlanRepeat")
    private String applicationName;

    // If just the website hostname is typed, redirect to landing page.
    @GetMapping("/")
    public String redirectToLanding() {
        return "redirect:/sleepplanrepeat/landing";
    }

    // Landing page. Links to user sign-in and registration.
    @GetMapping("/landing")
    public String getLanding() {
        return "landing";
    }

    // Calendar page. Updated with global and user events.
    // Redirected to after sign-in.
    @GetMapping("/calendar")
    public String getCalendar(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model, Authentication authentication)
    {
        // Display current month in schedule by default.
        YearMonth currentMonth;
        if (month != null && year != null) {
            currentMonth = YearMonth.of(year, month);
        } else {
            currentMonth = YearMonth.now();
        }

        // Format month.
        String formattedMonth = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        model.addAttribute("currentMonth", formattedMonth);

        // Add current month and year for reference in page navigation.
        model.addAttribute("currentMonthValue", currentMonth.getMonthValue());
        model.addAttribute("currentYear", currentMonth.getYear());

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

    // Calendar navigation. Leads to month before currently selected month.
    @GetMapping("/calendar/previous")
    public String getPreviousMonth(@RequestParam(required = false) Integer month,
                                   @RequestParam(required = false) Integer year,
                                   Model model, Authentication authentication) {
        YearMonth currentMonth;

        // Parse the month and year if provided.
        if (month != null && year != null) {
            currentMonth = YearMonth.of(year, month);
        } else {
            currentMonth = YearMonth.now();
        }

        // Get the previous month.
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Redirect to the calendar under the new month.
        return "redirect:/sleepplanrepeat/calendar?month=" + previousMonth.getMonthValue() + "&year=" + previousMonth.getYear();
    }

    // Calendar navigation. Leads to month after currently selected month.
    @GetMapping("/calendar/next")
    public String getNextMonth(@RequestParam(required = false) Integer month,
                               @RequestParam(required = false) Integer year,
                               Model model, Authentication authentication) {
        YearMonth currentMonth;

        // Parse the month and year if provided.
        if (month != null && year != null) {
            currentMonth = YearMonth.of(year, month);
        } else {
            currentMonth = YearMonth.now();
        }

        // Get the next month.
        YearMonth nextMonth = currentMonth.plusMonths(1);

        // Redirect to the calendar under the new month.
        return "redirect:/sleepplanrepeat/calendar?month=" + nextMonth.getMonthValue() + "&year=" + nextMonth.getYear();
    }

    // Calendar navigation. Pressing "Today" should update the calendar with the current month.
    @GetMapping("/calendar/today")
    public String getCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        return "redirect:/sleepplanrepeat/calendar?month=" + currentMonth.getMonthValue() + "&year=" + currentMonth.getYear();
    }

    // Returns information about events happening during specific day.
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

            return "day";

        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Invalid date format. Please use YYYY-MM-DD.");
            model.addAttribute("selectedDate", LocalDate.now());
            model.addAttribute("events", new ArrayList<Event>());
            return "day";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            model.addAttribute("selectedDate", LocalDate.now());
            model.addAttribute("events", new ArrayList<Event>());
            return "day";
        }
    }

    // Returns information about events happening during specific month.
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



// retrieves the user's username, and fetches their user details and their events, then it creates a personalized msg
// with Gemini AI based on the user's name and the events that they have coming up, and then returns the calendar view
// to be rendered.
    @GetMapping("/calendar")
    public String showCalendar(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.getUserByUsername(username);
        List<Event> userEvents = eventService.findEventsByUserId(user.getId());
        String personalizedMessage = geminiService.getPersonalizedMessage(username, userEvents);

        model.addAttribute("personalizedMessage", personalizedMessage);
        model.addAttribute("events", userEvents);

        return "calendar";
    }

}
