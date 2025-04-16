package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class EventService {

    private final EventRepository eventRepository;

    // Utilize constructor injection.
    private EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Retrieve all Events from the repository.
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Save an Event to the repository.
    public boolean saveEvent(Event event) {

        // Check over essential Event fields.
        if (event == null || event.getDescription() == null || event.getTitle() == null) {
            return false;
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
        eventRepository.save(event);
        return true;
    }

    // Find events given a specific date range.
    public List<Event> findEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findGlobalEventsByDateRange(start, end);
    }

    // Find user-specific events based on ID.
    public List<Event> findEventsByUserId(int userId) {
        return eventRepository.findByUserId(userId);
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
}
