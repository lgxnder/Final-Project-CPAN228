package com.humber.sleepPlanRepeat;

import com.humber.sleepPlanRepeat.services.EventService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SleepPlanRepeatApplication implements CommandLineRunner {

	// Constructor injecton
	private final EventService eventService;

	public SleepPlanRepeatApplication(EventService eventService) {
		this.eventService = eventService;
	}

	public static void main(String[] args) {
		SpringApplication.run(SleepPlanRepeatApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

	}

}
