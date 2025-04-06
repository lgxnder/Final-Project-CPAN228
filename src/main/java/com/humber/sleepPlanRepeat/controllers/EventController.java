package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import com.humber.sleepPlanRepeat.services.EventService;
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

    // Constructor injection
    public EventController(
            EventRepository eventRepository,
            UserRepository userRepository,
            EventService eventService
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Value("sleepPlanRepeat")
    private String applicationName;

    // View an event
    @GetMapping("/view/{id}")
    public String viewEvent(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Optional<Event> eventOpt = eventRepository.findById(id);

            if (eventOpt.isPresent()) {
                Event event = eventOpt.get();
                model.addAttribute("event", event);

                // Check if the current user is the owner of this event
                boolean isOwner = false;
                if (authentication != null && authentication.isAuthenticated() && event.getUser() != null) {
                    String username = authentication.getName();
                    Optional<User> userOpt = userRepository.findByUsername(username);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        isOwner = user.getId() == event.getUser().getId();
                    }
                }

                model.addAttribute("isOwner", isOwner);
                return "event-view";
            } else {
                model.addAttribute("error", "Event not found");
                return "redirect:/sleepplanrepeat/calendar";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error viewing event: " + e.getMessage());
            return "redirect:/sleepplanrepeat/calendar";
        }
    }

    // Display create event form
    @GetMapping("/create")
    public String createEventForm(
            @RequestParam(required = false) String date,
            Model model,
            Authentication authentication
    ) {
        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login?message=Please log in to create events";
        }

        Event event = new Event();

        // If a date parameter is provided, set it as the event start time
        if (date != null && !date.isEmpty()) {
            LocalDateTime startTime = LocalDateTime.parse(date + "T00:00:00");
            event.setStartTime(startTime);
            event.setEndTime(startTime.plusHours(1));
        } else {
            // Default to current date and time
            LocalDateTime now = LocalDateTime.now();
            event.setStartTime(now);
            event.setEndTime(now.plusHours(1));
        }

        model.addAttribute("event", event);
        model.addAttribute("isEdit", false);
        model.addAttribute("isGlobal", false);
        return "event-form";
    }

    // Process event creation
    @PostMapping("/create")
    public String createEvent(
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTime") String endTime,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Parse date and time strings to LocalDateTime
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate parsedStartDate = LocalDate.parse(startDate, dateFormatter);
            LocalTime parsedStartTime = LocalTime.parse(startTime, timeFormatter);
            LocalDateTime startDateTime = LocalDateTime.of(parsedStartDate, parsedStartTime);

            LocalDate parsedEndDate = LocalDate.parse(endDate, dateFormatter);
            LocalTime parsedEndTime = LocalTime.parse(endTime, timeFormatter);
            LocalDateTime endDateTime = LocalDateTime.of(parsedEndDate, parsedEndTime);

            // Set the parsed date-times on the event
            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);

            // Set the user if authenticated
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);

                if (userOpt.isPresent()) {
                    event.setUser(userOpt.get());
                }
            }

            // Save the event
            boolean success = eventService.saveEvent(event);

            if (success) {
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

    // Display edit event form
    @GetMapping("/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Event> eventOpt = eventRepository.findById(id);

        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();

            // Check if user is authorized to edit this event
            if (authentication != null && authentication.isAuthenticated() && event.getUser() != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);

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

            // If user is not authorized
            return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to edit this event";
        } else {
            return "redirect:/sleepplanrepeat/calendar?error=Event not found";
        }
    }

    // Process event update
    @PostMapping("/edit/{id}")
    public String updateEvent(
            @PathVariable Long id,
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTime") String endTime,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Optional<Event> existingEventOpt = eventRepository.findById(id);

            if (existingEventOpt.isPresent()) {
                Event existingEvent = existingEventOpt.get();

                // Check if user is authorized to edit this event
                if (authentication != null && authentication.isAuthenticated() && existingEvent.getUser() != null) {
                    String username = authentication.getName();
                    Optional<User> userOpt = userRepository.findByUsername(username);

                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        if (user.getId() == existingEvent.getUser().getId()) {
                            // Parse date and time strings to LocalDateTime
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                            LocalDate parsedStartDate = LocalDate.parse(startDate, dateFormatter);
                            LocalTime parsedStartTime = LocalTime.parse(startTime, timeFormatter);
                            LocalDateTime startDateTime = LocalDateTime.of(parsedStartDate, parsedStartTime);

                            LocalDate parsedEndDate = LocalDate.parse(endDate, dateFormatter);
                            LocalTime parsedEndTime = LocalTime.parse(endTime, timeFormatter);
                            LocalDateTime endDateTime = LocalDateTime.of(parsedEndDate, parsedEndTime);

                            // Update date and time
                            existingEvent.setStartTime(startDateTime);
                            existingEvent.setEndTime(endDateTime);

                            // Update other fields
                            existingEvent.setTitle(event.getTitle());
                            existingEvent.setDescription(event.getDescription());

                            // Save the updated event
                            eventRepository.save(existingEvent);

                            redirectAttributes.addFlashAttribute("message", "Event updated successfully!");
                            return "redirect:/sleepplanrepeat/events/view/" + id;
                        }
                    }
                }

                // If user is not authorized
                return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to edit this event";
            } else {
                return "redirect:/sleepplanrepeat/calendar?error=Event not found";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating event: " + e.getMessage());
            return "redirect:/sleepplanrepeat/events/edit/" + id;
        }
    }

    // Delete an event
    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Optional<Event> eventOpt = eventRepository.findById(id);

        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();

            // Check if user is authorized to delete this event
            if (authentication != null && authentication.isAuthenticated() && event.getUser() != null) {
                String username = authentication.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    if (user.getId() == event.getUser().getId()) {
                        // Delete the event
                        eventRepository.delete(event);

                        redirectAttributes.addFlashAttribute("message", "Event deleted successfully!");
                        return "redirect:/sleepplanrepeat/calendar";
                    }
                }
            }

            // If user is not authorized
            return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to delete this event";
        } else {
            return "redirect:/sleepplanrepeat/calendar?error=Event not found";
        }
    }

    // Create a global event (admin only, could be expanded later)
    @GetMapping("/create-global")
    public String createGlobalEventForm(Model model, Authentication authentication) {
        // For now, assume only admins can create global events
        // In a real app, you'd check for admin role

        Event event = new Event();
        LocalDateTime now = LocalDateTime.now();
        event.setStartTime(now);
        event.setEndTime(now.plusHours(1));

        model.addAttribute("event", event);
        model.addAttribute("isGlobal", true);
        model.addAttribute("isEdit", false);
        return "event-form";
    }

    // Process global event creation
    @PostMapping("/create-global")
    public String createGlobalEvent(
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTime") String endTime,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Parse date and time strings to LocalDateTime
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate parsedStartDate = LocalDate.parse(startDate, dateFormatter);
            LocalTime parsedStartTime = LocalTime.parse(startTime, timeFormatter);
            LocalDateTime startDateTime = LocalDateTime.of(parsedStartDate, parsedStartTime);

            LocalDate parsedEndDate = LocalDate.parse(endDate, dateFormatter);
            LocalTime parsedEndTime = LocalTime.parse(endTime, timeFormatter);
            LocalDateTime endDateTime = LocalDateTime.of(parsedEndDate, parsedEndTime);

            // Set the parsed date-times on the event
            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);

            // Global events have no user
            event.setUser(null);

            // Save the event
            boolean success = eventService.saveEvent(event);

            if (success) {
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
}