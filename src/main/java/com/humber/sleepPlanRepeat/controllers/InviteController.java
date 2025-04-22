package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.Invitation;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository; // Add UserRepository to look up users
import com.humber.sleepPlanRepeat.services.EventService;
import com.humber.sleepPlanRepeat.services.InviteService;
import com.humber.sleepPlanRepeat.services.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final InviteService inviteService;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Constructor injection
    public InviteController(InviteService inviteService, EventService eventService,
                            NotificationService notificationService, EventRepository eventRepository,
                            UserRepository userRepository) {
        this.inviteService = inviteService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // Send an invitation
    @PostMapping("/send")
    public Invitation sendInvite(@RequestParam Long eventId, @RequestParam String inviteeEmail, Principal principal) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            throw new RuntimeException("Event not found");
        }

        Event event = optionalEvent.get();

        // Basic permission check: ensure the logged-in user is the owner of the event
        if (!event.getUser().getUsername().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized to send invites for this event");
        }

        Invitation invitation = new Invitation();
        invitation.setEvent(event);
        invitation.setInviteeEmail(inviteeEmail);
        invitation.setStatus(Invitation.InvitationStatus.PENDING);
        Invitation savedInvite = inviteService.createInvitation(invitation);

        // Optionally notify via email
        notificationService.sendUpcomingEventNotification(event.getUser(), event);

        return savedInvite;
    }

    // Accept an invitation
    @PostMapping("/accept/{invitationId}")
    public Invitation acceptInvite(@PathVariable Long invitationId, Principal principal) {
        Invitation invitation = inviteService.acceptInvitation(invitationId);

        // Ensure the logged-in user is the one being invited
        if (!invitation.getInviteeEmail().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized to accept this invitation");
        }

        // Clone the event into the invitee's calendar if needed
        Event originalEvent = invitation.getEvent();

        // Create a shared copy of the event
        Event sharedCopy = new Event();
        sharedCopy.setTitle(originalEvent.getTitle());
        sharedCopy.setDescription(originalEvent.getDescription());
        sharedCopy.setStartTime(originalEvent.getStartTime());
        sharedCopy.setEndTime(originalEvent.getEndTime());
        sharedCopy.setOriginalEvent(originalEvent);
        sharedCopy.setShared(true);

        // Lookup the invitee user by email (from the principal or invitation)
        User invitee = userRepository.findByEmail(invitation.getInviteeEmail())
                .orElseThrow(() -> new RuntimeException("User not found for email: " + invitation.getInviteeEmail()));

        // Assign the invitee to the shared event
        sharedCopy.setUser(invitee);

        // Save the shared event to the repository
        eventService.saveEvent(sharedCopy);

        return invitation;
    }


    // Reject an invitation
    @PostMapping("/reject/{invitationId}")
    public Invitation rejectInvite(@PathVariable Long invitationId) {
        return inviteService.rejectInvitation(invitationId);
    }

    // List all invites for the currently logged-in user
    @GetMapping("/my")
    public List<Invitation> getMyInvites(Principal principal) {
        return inviteService.getAcceptedOrPendingInvitationsByInviteeEmail(principal.getName());
    }
}
