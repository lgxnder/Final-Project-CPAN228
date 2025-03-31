package com.humber.sleepPlanRepeat;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
public class SleepPlanRepeatApplication implements CommandLineRunner {

	// Constructor injection
	private final EventService eventService;
	private final EventRepository eventRepository;

	public SleepPlanRepeatApplication(EventService eventService, EventRepository eventRepository) {
		this.eventService = eventService;
		this.eventRepository = eventRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(SleepPlanRepeatApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// check out library "jollyday" to replace manual insertion
		// add dependency to pom.xmls
		eventRepository.save(new Event("Christmas",
				LocalDateTime.of(2025, 12, 25, 0, 0),
				LocalDateTime.of(2025, 12, 25, 23, 59),
				"Celebrate Christmas!",
		null));	// No user = global event

		eventRepository.save(new Event("St. Patrick's Day",
				LocalDateTime.of(2025, 3, 17, 0, 0),
				LocalDateTime.of(2025, 3, 17, 23, 59),
				"Wear green!",
				null));
	}
}
