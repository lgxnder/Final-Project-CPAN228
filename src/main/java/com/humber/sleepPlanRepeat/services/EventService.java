package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User; // Remove unused import
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@Service
public class EventService {

    private final EventRepository eventRepository;

    // Utilize constructor injection.
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Retrieve all Events from the repository.
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Save an Event to the repository.
    public Event saveEvent(Event event) { // Changed return type to Event

        // Check over essential Event fields.
        if (event == null || event.getDescription() == null || event.getTitle() == null) {
            return null;
        }

        // Handle missing start time by setting it to the current time.
        if (event.getStartTime() == null) {
            event.setStartTime(LocalDateTime.now());
        }

        // Ensure end time of Event exists, and is after the start time.
        // If the end time is missing, default to +1 hour after the start time.
        if (event.getEndTime() == null || event.getEndTime().isBefore(event.getStartTime())) {
            event.setEndTime(event.getStartTime().plusHours(1));
        }

        // Validation passed. Save Event object to repository.
        return eventRepository.save(event); // returns the saved event
    }

    // Find events given a specific date range.
    public List<Event> findEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findGlobalEventsByDateRange(start, end);
    }

    // Find user-specific events based on ID.
    public List<Event> findEventsByUserId(int userId) {
        return eventRepository.findByUserId(userId);
    }

    //Added saveGlobalEvent
    public Event saveGlobalEvent(Event event) {
        // Check over essential Event fields.
        if (event == null || event.getDescription() == null || event.getTitle() == null) {
            return null; // Or throw an exception, depending on your error handling policy
        }

        // Handle missing start time by setting it to the current time.
        if (event.getStartTime() == null) {
            event.setStartTime(LocalDateTime.now());
        }

        // Ensure end time of Event exists, and is after the start time.
        // If the end time is missing, default to +1 hour after the start time.
        if (event.getEndTime() == null || event.getEndTime().isBefore(event.getStartTime())) {
            event.setEndTime(event.getStartTime().plusHours(1));
        }
        return eventRepository.save(event);

    }


    // parsing date and time strings into LocalDateTime objects for gemini event creation
    public LocalDateTime parseDateTime(String date, String time) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate parsedDate = LocalDate.parse(date, dateFormatter);
        LocalTime parsedTime = LocalTime.parse(time, timeFormatter);
        return LocalDateTime.of(parsedDate, parsedTime);
    }

    public boolean isEndTimeValid(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return endDateTime.isAfter(startDateTime);
    }



    @Transactional
    // updating an original event and all its shared copies
    public Event updateEventAndSharedCopies(Long eventId, Event updatedData) {
        Optional<Event> optionalOriginal = eventRepository.findById(eventId);
        if (optionalOriginal.isEmpty()) {
            throw new RuntimeException("Original event not found");
        }

        Event original = optionalOriginal.get();

        // Update fields on the original event
        original.setTitle(updatedData.getTitle());
        original.setStartTime(updatedData.getStartTime());
        original.setEndTime(updatedData.getEndTime());
        original.setDescription(updatedData.getDescription());
        original.setExternalLink(updatedData.getExternalLink());
        original.setFocusTag(updatedData.getFocusTag());
        original.setColor(updatedData.getColor());
        original.setPriority(updatedData.getPriority());

        // Save the updated original
        Event savedOriginal = eventRepository.save(original);

        // Propagate the same changes to all shared copies
        for (Event sharedCopy : original.getSharedCopies()) {
            sharedCopy.setTitle(original.getTitle());
            sharedCopy.setStartTime(original.getStartTime());
            sharedCopy.setEndTime(original.getEndTime());
            sharedCopy.setDescription(original.getDescription());
            sharedCopy.setExternalLink(original.getExternalLink());
            sharedCopy.setFocusTag(original.getFocusTag());
            sharedCopy.setColor(original.getColor());
            sharedCopy.setPriority(original.getPriority());

            eventRepository.save(sharedCopy);
        }

        return savedOriginal;
    }



}
