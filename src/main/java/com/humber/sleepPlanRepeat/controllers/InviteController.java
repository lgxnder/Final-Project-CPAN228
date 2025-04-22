package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.Invitation;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.services.EventService;
import com.humber.sleepPlanRepeat.services.InviteService;
import com.humber.sleepPlanRepeat.services.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    @Autowired
    private InviteService inviteService;

    @Autowired
    private EventService eventService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EventRepository eventRepository;

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
    public Invitation acceptInvite(@PathVariable Long invitationId) {
        Invitation invitation = inviteService.acceptInvitation(invitationId);

        // Clone the event into the invitee's calendar if needed (pseudo-logic)
        Event originalEvent = invitation.getEvent();
        Event sharedCopy = new Event();
        sharedCopy.setTitle(originalEvent.getTitle());
        sharedCopy.setDescription(originalEvent.getDescription());
        sharedCopy.setStartTime(originalEvent.getStartTime());
        sharedCopy.setEndTime(originalEvent.getEndTime());
        sharedCopy.setOriginalEvent(originalEvent);
        sharedCopy.setShared(true);
        // You would normally set the invitee user here using email -> User lookup
        // sharedCopy.setUser(userRepository.findByEmail(invitation.getInviteeEmail()));

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
