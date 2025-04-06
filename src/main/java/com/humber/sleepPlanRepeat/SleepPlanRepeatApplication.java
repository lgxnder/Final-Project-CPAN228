package com.humber.sleepPlanRepeat;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;

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
	// Only add sample events if none exist
		if (eventRepository.count() == 0) {
			// Current year
			int currentYear = Year.now().getValue();

			// Add global holidays and events

			// April (current month)
			eventRepository.save(new Event(
					"Earth Day",
					LocalDateTime.of(currentYear, Month.APRIL, 22, 0, 0),
					LocalDateTime.of(currentYear, Month.APRIL, 22, 23, 59),
					"Global celebration for environmental protection",
					null
			));

			// May
			eventRepository.save(new Event(
					"Mother's Day",
					LocalDateTime.of(currentYear, Month.MAY, 12, 0, 0),
					LocalDateTime.of(currentYear, Month.MAY, 12, 23, 59),
					"Holiday honoring mothers",
					null
			));

			eventRepository.save(new Event(
					"Memorial Day",
					LocalDateTime.of(currentYear, Month.MAY, 27, 0, 0),
					LocalDateTime.of(currentYear, Month.MAY, 27, 23, 59),
					"Holiday honoring military personnel",
					null
			));

			// June
			eventRepository.save(new Event(
					"Father's Day",
					LocalDateTime.of(currentYear, Month.JUNE, 16, 0, 0),
					LocalDateTime.of(currentYear, Month.JUNE, 16, 23, 59),
					"Holiday honoring fathers",
					null
			));

			// July
			eventRepository.save(new Event(
					"Independence Day",
					LocalDateTime.of(currentYear, Month.JULY, 4, 0, 0),
					LocalDateTime.of(currentYear, Month.JULY, 4, 23, 59),
					"US Independence Day celebration",
					null
			));

			// October
			eventRepository.save(new Event(
					"Halloween",
					LocalDateTime.of(currentYear, Month.OCTOBER, 31, 0, 0),
					LocalDateTime.of(currentYear, Month.OCTOBER, 31, 23, 59),
					"Annual celebration of all things spooky",
					null
			));

			// November
			eventRepository.save(new Event(
					"Thanksgiving",
					LocalDateTime.of(currentYear, Month.NOVEMBER, 28, 0, 0),
					LocalDateTime.of(currentYear, Month.NOVEMBER, 28, 23, 59),
					"Holiday for giving thanks",
					null
			));

			// December
			eventRepository.save(new Event(
					"Christmas",
					LocalDateTime.of(currentYear, Month.DECEMBER, 25, 0, 0),
					LocalDateTime.of(currentYear, Month.DECEMBER, 25, 23, 59),
					"Christmas Day celebration",
					null
			));

			eventRepository.save(new Event(
					"New Year's Eve",
					LocalDateTime.of(currentYear, Month.DECEMBER, 31, 20, 0),
					LocalDateTime.of(currentYear, Month.DECEMBER, 31, 23, 59),
					"New Year's Eve celebration",
					null
			));

			System.out.println("Sample global events have been added!");
		}
	}
}
