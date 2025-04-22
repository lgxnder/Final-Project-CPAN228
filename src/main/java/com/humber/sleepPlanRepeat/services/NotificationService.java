package com.humber.sleepPlanRepeat.services;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitationEmail(String recipientEmail, String inviteLink, String eventTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom("examplecreated@gmail.com");
        message.setSubject("You're Invited to: " + eventTitle);

        String body = String.format(
                "Hello,\n\nYou've been invited to an event titled \"%s\".\n" +
                        "Click the link below to view the event and respond:\n\n%s\n\n" +
                        "Best,\nSleepPlanRepeat Team", eventTitle, inviteLink);

        message.setText(body);
        mailSender.send(message);

        System.out.println("Invite email sent to: " + recipientEmail);
    }

    public void sendUpcomingEventNotification(User user, Event event) {
        if (user.isEnableEmailNotifications() && user.getEmail() != null && !user.getEmail().isEmpty()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setFrom("examplecreated@gmail.com");
            message.setSubject("Upcoming Event Reminder: " + event.getTitle());

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a z");
            ZoneId userTimeZone = ZoneId.systemDefault(); // You might want to store user's timezone

            String startTimeFormatted = event.getStartTime().atZone(userTimeZone).format(timeFormatter);

            String body = String.format("Hello %s,\n\nThis is a reminder for your upcoming event:\n\n" +
                            "Title: %s\n" +
                            "Start Time: %s\n" +
                            "Description: %s\n\n" +
                            "We look forward to seeing you there!\n\n" +
                            "Sincerely,\nThe SleepPlanRepeat Team",
                    user.getUsername(),
                    event.getTitle(),
                    startTimeFormatted,
                    event.getDescription() != null ? event.getDescription() : "No description provided");

            message.setText(body);

            mailSender.send(message);
            System.out.println("Email notification sent to: " + user.getEmail() + " for event: " + event.getTitle());
        } else {
            System.out.println("Email notifications are disabled or email not set for user: " + user.getUsername());
        }
    }
}