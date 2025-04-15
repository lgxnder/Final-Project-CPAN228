package com.humber.sleepPlanRepeat.config;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import com.humber.sleepPlanRepeat.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public SchedulerConfig(EventRepository eventRepository, UserRepository userRepository, NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 * * * * *") // runs every minute
    public void checkUpcomingEventsAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notificationThreshold = now.plusMinutes(15); // Send notifications 15 minutes before

        List<Event> upcomingEvents = eventRepository.findEventsStartingBetween(now, notificationThreshold);

        for (Event event : upcomingEvents) {
            User user = event.getUser();
            if (user != null) {
                notificationService.sendUpcomingEventNotification(user, event);
            }
        }
    }
}