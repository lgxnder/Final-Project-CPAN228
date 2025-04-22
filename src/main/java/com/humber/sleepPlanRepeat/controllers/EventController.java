package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import com.humber.sleepPlanRepeat.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/sleepplanrepeat/events")
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final UserService userService;

    // Constructor injection.
    public EventController(
            EventRepository eventRepository,
            UserRepository userRepository,
            EventService eventService,
            UserService userService
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Value("sleepPlanRepeat")
    private String applicationName;

    // View a specific event.
    @GetMapping("/view/{id}")
    public String viewEvent(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Optional<Event> eventOpt = eventRepository.findById(id);

            if (eventOpt.isPresent()) {
                Event event = eventOpt.get();
                model.addAttribute("event", event);

                // Check if current user "owns" the event.
                boolean isOwner = false;
                if (authentication != null && authentication.isAuthenticated() && event.getUser() != null) {
                    String username = authentication.getName();
                    Optional<User> userOpt = userRepository.findByUsername(username);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        isOwner = user.getId() == event.getUser().getId();
                    }
                }

                // If user "owns" event, display it.
                model.addAttribute("isOwner", isOwner);
                return "event-view";

                // Otherwise, they should not be able to access it.
            } else {
                model.addAttribute("error", "Event not found");
                return "redirect:/sleepplanrepeat/calendar";
            }
            // Any errors result in an error message and then redirect back to calendar.
        } catch (Exception e) {
            model.addAttribute("error", "Error viewing event: " + e.getMessage());
            return "redirect:/sleepplanrepeat/calendar";
        }
    }

    // Create-event form.
    @GetMapping("/create")
    public String createEventForm(
            @RequestParam(required = false) String date,
            Model model,
            Authentication authentication
    ) {

        // Check if user is authenticated.
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login?message=Please log in to create events";
        }

        Event event = new Event();

        // If a date parameter is provided by the user, set it as the event's start time.
        if (date != null && !date.isEmpty()) {
            LocalDateTime startTime = LocalDateTime.parse(date + "T00:00:00");
            event.setStartTime(startTime);
            event.setEndTime(startTime.plusHours(1));

            // Otherwise, default to the current date and time.
        } else {
            LocalDateTime now = LocalDateTime.now();
            event.setStartTime(now);
            event.setEndTime(now.plusHours(1));
        }

        model.addAttribute("event", event);
        model.addAttribute("isEdit", false);
        model.addAttribute("isGlobal", false);
        return "event-form";
    }

    // Process event creation endpoint.
    @PostMapping("/create")
    public String createEvent(
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTimeInput") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTimeInput") String endTime,
            @RequestParam(value = "externalLink", required = false) String externalLink,
            @RequestParam(value = "focusTag", required = false) String focusTag,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Parse and format DateTime strings into LocalTime and LocalDate.
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate parsedStartDate = LocalDate.parse(startDate, dateFormatter);
            LocalTime parsedStartTime = LocalTime.parse(startTime, timeFormatter);
            LocalDateTime startDateTime = LocalDateTime.of(parsedStartDate, parsedStartTime);

            LocalDate parsedEndDate = LocalDate.parse(endDate, dateFormatter);
            LocalTime parsedEndTime = LocalTime.parse(endTime, timeFormatter);
            LocalDateTime endDateTime = LocalDateTime.of(parsedEndDate, parsedEndTime);

            // Validate that the end time occurs after the start time of the event.
            if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
                redirectAttributes.addFlashAttribute("error", "End time must be after start time");
                return "redirect:/sleepplanrepeat/events/create";
            }

            // Update event with parsed and formatted times.
            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);
            event.setExternalLink(externalLink);
            event.setFocusTag(focusTag);

            // Check if the user is authenticated properly.
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);

                // Check to ensure user is present in the database.
                if (userOpt.isPresent()) {
                    event.setUser(userOpt.get());
                }
            }

            // Save the event.
            Event savedEvent = eventService.saveEvent(event);

            // Redirect in different ways according to outcome of saving the Event.
            if (savedEvent != null) {
                redirectAttributes.addFlashAttribute("message", "Event created successfully!");
                return "redirect:/sleepplanrepeat/calendar";
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create event. Please check your inputs.");
                return "redirect:/sleepplanrepeat/events/create";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating event: " + e.getMessage());
            return "redirect:/sleepplanrepeat/events/create";
        }
    }

    // Edit an event form.
    @GetMapping("/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Event> eventOpt = eventRepository.findById(id);

        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();

            // Check if user is authenticated to edit this specific event.
            if (authentication != null && authentication.isAuthenticated() && event.getUser() != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);

                // Check to ensure user is present in the database.
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    if (user.getId() == event.getUser().getId()) {
                        model.addAttribute("event", event);
                        model.addAttribute("isEdit", true);
                        model.addAttribute("isGlobal", event.getUser() == null);
                        return "event-form";
                    }
                }
            }

            // If user is not authorized, then return back to calendar view.
            return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to edit this event";

        } else {
            // If event could not be found.
            return "redirect:/sleepplanrepeat/calendar?error=Event not found";
        }
    }

    // Process the edit-event submission.
    @PostMapping("/edit/{id}")
    public String updateEvent(
            @PathVariable Long id,
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTimeInput") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTimeInput") String endTime,
            @RequestParam(value = "externalLink", required = false) String externalLink,
            @RequestParam(value = "focusTag", required = false) String focusTag,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Validate that the event is present in the database.
            Optional<Event> existingEventOpt = eventRepository.findById(id);

            if (existingEventOpt.isPresent()) {
                Event existingEvent = existingEventOpt.get();

                // Check if user is authorized to edit this event.
                if (authentication != null && authentication.isAuthenticated() && existingEvent.getUser() != null) {
                    String username = authentication.getName();
                    Optional<User> userOpt = userRepository.findByUsername(username);

                    // Validate that the user is present in the database.
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        if (user.getId() == existingEvent.getUser().getId()) {

                            // Parse and format DateTime strings into LocalTime and LocalDate.
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                            LocalDate parsedStartDate = LocalDate.parse(startDate, dateFormatter);
                            LocalTime parsedStartTime = LocalTime.parse(startTime, timeFormatter);
                            LocalDateTime startDateTime = LocalDateTime.of(parsedStartDate, parsedStartTime);

                            LocalDate parsedEndDate = LocalDate.parse(endDate, dateFormatter);
                            LocalTime parsedEndTime = LocalTime.parse(endTime, timeFormatter);
                            LocalDateTime endDateTime = LocalDateTime.of(parsedEndDate, parsedEndTime);

                            // Validate that the end time occurs after the start time of the event.
                            if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
                                redirectAttributes.addFlashAttribute("error", "End time must be after start time");
                                return "redirect:/sleepplanrepeat/events/edit/" + id;
                            }

                            // Update event with parsed and formatted date and time.
                            existingEvent.setStartTime(startDateTime);
                            existingEvent.setEndTime(endDateTime);

                            // Update event's title and description fields.
                            existingEvent.setTitle(event.getTitle());
                            existingEvent.setDescription(event.getDescription());
                            existingEvent.setExternalLink(externalLink);
                            existingEvent.setFocusTag(focusTag);
                            existingEvent.setColor(event.getColor()); // Set the color from the form
                            existingEvent.setPriority(event.getPriority());

                            // Save the updated event.
                            eventService.saveEvent(existingEvent);

                            redirectAttributes.addFlashAttribute("message", "Event updated successfully!");

                            // Redirect to the event-view.
                            return "redirect:/sleepplanrepeat/events/view/" + id;
                        }
                    }
                }
                // If user is not authorized, send error message and redirect to calendar.
                return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to edit this event";
            } else {
                // If event could not be found, direct to calendar as well.
                return "redirect:/sleepplanrepeat/calendar?error=Event not found";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating event: " + e.getMessage());
            return "redirect:/sleepplanrepeat/events/edit/" + id;
        }
    }


    // Delete an event by id.
    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Optional<Event> eventOpt = eventRepository.findById(id);

        // Ensure event is in database.
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();

            // Check if user is authorized to delete this event.
            if (authentication != null && authentication.isAuthenticated() && event.getUser() != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);

                // Ensure user is in database.
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    if (user.getId() == event.getUser().getId()) {
                        // Delete the event.
                        eventRepository.delete(event);

                        redirectAttributes.addFlashAttribute("message", "Event deleted successfully!");
                        return "redirect:/sleepplanrepeat/calendar";
                    }
                }
            }

            // If user is not authorized, redirect.
            return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to delete this event";
        } else {

            // If event could not be found, redirect.
            return "redirect:/sleepplanrepeat/calendar?error=Event not found";
        }
    }

    // Create a global event that can be seen by all users in their calendars.
    // ADMIN-ONLY
    @GetMapping("/create-global")
    public String createGlobalEventForm(Model model, Authentication authentication) {
        // Method must be authenticated beforehand in order to run.

        Event event = new Event();
        LocalDateTime now = LocalDateTime.now();
        event.setStartTime(now);
        event.setEndTime(now.plusHours(1));

        model.addAttribute("event", event);
        model.addAttribute("isGlobal", true);
        model.addAttribute("isEdit", false);
        return "event-form";
    }

    // ADMIN-ONLY
    // Process global event creation.
    @PostMapping("/create-global")
    public String createGlobalEvent(
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTimeInput") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTimeInput") String endTime,
            @RequestParam(value = "externalLink", required = false) String externalLink,
            @RequestParam(value = "focusTag", required = false) String focusTag,
            @RequestParam(value = "priority", required = false) String priority,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Parse and format DateTime strings into LocalTime and LocalDate.
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate parsedStartDate = LocalDate.parse(startDate, dateFormatter);
            LocalTime parsedStartTime = LocalTime.parse(startTime, timeFormatter);
            LocalDateTime startDateTime = LocalDateTime.of(parsedStartDate, parsedStartTime);

            LocalDate parsedEndDate = LocalDate.parse(endDate, dateFormatter);
            LocalTime parsedEndTime = LocalTime.parse(endTime, timeFormatter);
            LocalDateTime endDateTime = LocalDateTime.of(parsedEndDate, parsedEndTime);

            // Validate that the end time occurs after the start time of the event.
            if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
                redirectAttributes.addFlashAttribute("error", "End time must be after start time");
                return "redirect:/sleepplanrepeat/events/create-global";
            }

            // Update event with parsed and formatted dates and times.
            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);
            event.setExternalLink(externalLink);
            event.setFocusTag(focusTag);
            event.setColor(event.getColor()); // Set the color from the form
            event.setPriority(priority); // Set priority from the form

            // Ensure that no users are associated with this event as it is global.
            event.setUser(null);

            // Save the event.
            Event savedEvent = eventService.saveGlobalEvent(event);

            if (savedEvent != null) {
                redirectAttributes.addFlashAttribute("message", "Global event created successfully!");
                return "redirect:/sleepplanrepeat/calendar";
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create global event. Please check your inputs.");
                return "redirect:/sleepplanrepeat/events/create-global";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating global event: " + e.getMessage());
            return "redirect:/sleepplanrepeat/events/create-global";
        }
    }


// Gemini Functionality Section in EventController

    // Private helper to get the authenticated user using UserService.
    private Optional<User> getAuthenticatedUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userService.getUserByUsername(auth.getName()));
    }

    // Private helper to parse a date and time string into a LocalDateTime.
    private LocalDateTime parseLocalDateTime(String dateStr, String timeStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        LocalTime time = LocalTime.parse(timeStr, timeFormatter);
        return LocalDateTime.of(date, time);
    }


    // Private helper to parse and validate date and time.
    private LocalDateTime parseDateTime(String date, String time) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate parsedDate = LocalDate.parse(date, dateFormatter);
        LocalTime parsedTime = LocalTime.parse(time, timeFormatter);
        return LocalDateTime.of(parsedDate, parsedTime);
    }

    // Private helper to validate event timing.
    private boolean isEndTimeValid(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return endDateTime.isAfter(startDateTime);
    }



    // Endpoint to update the original event and its shared copies
    @PutMapping("/{eventId}")
    public Event updateEventAndSharedCopies(@PathVariable Long eventId, @RequestBody Event updatedData) {
        return eventService.updateEventAndSharedCopies(eventId, updatedData);
    }
}

