package com.humber.sleepPlanRepeat.controllers;
import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import com.humber.sleepPlanRepeat.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/sleepplanrepeat/events")
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final UserService userService;

    @Value("sleepPlanRepeat")
    private String applicationName;

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

    @GetMapping("/view/{id}")
    public String viewEvent(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            model.addAttribute("event", event);
            boolean isOwner = isAuthenticatedAndOwnsEvent(authentication, event);
            model.addAttribute("isOwner", isOwner);
            return "event-view";
        } else {
            model.addAttribute("error", "Event not found");
            return "redirect:/sleepplanrepeat/calendar";
        }
    }

    @GetMapping("/create")
    public String createEventForm(@RequestParam(required = false) String date, Model model, Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login?message=Please log in to create events";
        }
        Event event = new Event();
        if (date != null && !date.isEmpty()) {
            LocalDateTime startTime = LocalDateTime.parse(date + "T00:00:00");
            event.setStartTime(startTime);
            event.setEndTime(startTime.plusHours(1));
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

    @PostMapping("/create")
    public String createEvent(
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTimeInput") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTimeInput") String endTime,
            @RequestParam(value = "externalLink", required = false) String externalLink, // Get externalLink
            @RequestParam(value = "focusTag", required = false) String focusTag,       // Get focusTag
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            LocalDateTime startDateTime = parseDateTime(startDate, startTime);
            LocalDateTime endDateTime = parseDateTime(endDate, endTime);

            if (!isEndTimeValid(startDateTime, endDateTime)) {
                redirectAttributes.addFlashAttribute("error", "End time must be after start time");
                return "redirect:/sleepplanrepeat/events/create";
            }

            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);
            event.setExternalLink(externalLink); // Set the externalLink
            event.setFocusTag(focusTag);         // Set the focusTag

            getAuthenticatedUser(authentication).ifPresent(event::setUser);

            if (eventService.saveEvent(event)) {
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

    @GetMapping("/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            if (isAuthenticatedAndOwnsEvent(authentication, event)) {
                model.addAttribute("event", event);
                model.addAttribute("isEdit", true);
                model.addAttribute("isGlobal", event.getUser() == null);
                return "event-form";
            } else {
                return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to edit this event";
            }
        } else {
            return "redirect:/sleepplanrepeat/calendar?error=Event not found";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateEvent(
            @PathVariable Long id,
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTimeInput") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTimeInput") String endTime,
            @RequestParam(value = "externalLink", required = false) String externalLink, // Get externalLink
            @RequestParam(value = "focusTag", required = false) String focusTag,       // Get focusTag
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Optional<Event> existingEventOpt = eventRepository.findById(id);
            if (existingEventOpt.isPresent()) {
                Event existingEvent = existingEventOpt.get();
                if (isAuthenticatedAndOwnsEvent(authentication, existingEvent)) {
                    LocalDateTime startDateTime = parseDateTime(startDate, startTime);
                    LocalDateTime endDateTime = parseDateTime(endDate, endTime);

                    if (!isEndTimeValid(startDateTime, endDateTime)) {
                        redirectAttributes.addFlashAttribute("error", "End time must be after start time");
                        return "redirect:/sleepplanrepeat/events/edit/" + id;
                    }

                    existingEvent.setTitle(event.getTitle());
                    existingEvent.setDescription(event.getDescription());
                    existingEvent.setStartTime(startDateTime);
                    existingEvent.setEndTime(endDateTime);
                    existingEvent.setExternalLink(externalLink); // Set the externalLink
                    existingEvent.setFocusTag(focusTag);         // Set the focusTag
                    existingEvent.setColor(event.getColor()); // Set the color from the form

                    eventRepository.save(existingEvent);
                    redirectAttributes.addFlashAttribute("message", "Event updated successfully!");
                    return "redirect:/sleepplanrepeat/events/view/" + id;
                } else {
                    return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to edit this event";
                }
            } else {
                return "redirect:/sleepplanrepeat/calendar?error=Event not found";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating event: " + e.getMessage());
            return "redirect:/sleepplanrepeat/events/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            if (isAuthenticatedAndOwnsEvent(authentication, event)) {
                eventRepository.delete(event);
                redirectAttributes.addFlashAttribute("message", "Event deleted successfully!");
                return "redirect:/sleepplanrepeat/calendar";
            } else {
                return "redirect:/sleepplanrepeat/calendar?error=You are not authorized to delete this event";
            }
        } else {
            return "redirect:/sleepplanrepeat/calendar?error=Event not found";
        }
    }

    @GetMapping("/create-global")
    public String createGlobalEventForm(Model model, Authentication authentication) {
        Event event = new Event();
        LocalDateTime now = LocalDateTime.now();
        event.setStartTime(now);
        event.setEndTime(now.plusHours(1));
        model.addAttribute("event", event);
        model.addAttribute("isGlobal", true);
        model.addAttribute("isEdit", false);
        return "event-form";
    }

    @PostMapping("/create-global")
    public String createGlobalEvent(
            @ModelAttribute("event") Event event,
            @RequestParam("startDate") String startDate,
            @RequestParam("startTimeInput") String startTime,
            @RequestParam("endDate") String endDate,
            @RequestParam("endTimeInput") String endTime,
            @RequestParam(value = "externalLink", required = false) String externalLink, // Get externalLink
            @RequestParam(value = "focusTag", required = false) String focusTag,       // Get focusTag
            RedirectAttributes redirectAttributes
    ) {
        try {
            LocalDateTime startDateTime = parseDateTime(startDate, startTime);
            LocalDateTime endDateTime = parseDateTime(endDate, endTime);

            if (!isEndTimeValid(startDateTime, endDateTime)) {
                redirectAttributes.addFlashAttribute("error", "End time must be after start time");
                return "redirect:/sleepplanrepeat/events/create-global";
            }

            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);
            event.setExternalLink(externalLink); // Set the externalLink
            event.setFocusTag(focusTag);         // Set the focusTag
            event.setColor(event.getColor()); // Set the color from the form
            event.setUser(null);

            if (eventService.saveEvent(event)) {
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

    private Optional<User> getAuthenticatedUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userService.getUserByUsername(auth.getName()));
    }

    private LocalDateTime parseDateTime(String dateStr, String timeStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        LocalTime time = LocalTime.parse(timeStr, timeFormatter);
        return LocalDateTime.of(date, time);
    }

    private boolean isEndTimeValid(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return endDateTime.isAfter(startDateTime);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private boolean isAuthenticatedAndOwnsEvent(Authentication authentication, Event event) {
        return isAuthenticated(authentication) && event.getUser() != null &&
                getAuthenticatedUser(authentication)
                        .map(user -> user.getId() == event.getUser().getId())
                        .orElse(false);
    }
}