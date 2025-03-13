package com.humber.sleepPlanRepeat.services;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service    // Designate class as a service.
public class EventService {

    // Utilize constructor injection.
    private final EventRepository eventRepository;

    private EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Retrieve all Events from the repository.
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Save an Event to the repository. Return boolean as int to indicate outcome.
    public int saveEvent(Event event) {
        if (event.getDescription() == null) {
            return 0;
        }
        // Perform validation.

        eventRepository.save(event);
        return 1;
        // Save Event object to repository.
    }
}
