package com.humber.sleepPlanRepeat.controllers;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.Invitation;
import com.humber.sleepPlanRepeat.models.Rsvpdto;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.EventRepository;
import com.humber.sleepPlanRepeat.repositories.UserRepository; // Add UserRepository to look up users
import com.humber.sleepPlanRepeat.services.EventService;
import com.humber.sleepPlanRepeat.services.InviteService;
import com.humber.sleepPlanRepeat.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sleepplanrepeat/api/invites")
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

        if (!invitation.getInviteeEmail().equalsIgnoreCase(principal.getName())) {
            throw new RuntimeException("Unauthorized to accept this invitation");
        }

        return invitation;
    }



    // Reject an invitation
    @PostMapping("/reject/{invitationId}")
    public Invitation rejectInvite(@PathVariable Long invitationId) {
        return inviteService.rejectInvitation(invitationId);
    }



    @GetMapping("/accept")
    public String acceptInviteByToken(@RequestParam String token, Principal principal) {
        try {
            inviteService.acceptInvitationByToken(token, principal.getName());
            return "redirect:/sleepplanrepeat/calendar?inviteAccepted=true";
        } catch (Exception e) {
            return "redirect:/sleepplanrepeat/calendar?error=" + e.getMessage();
        }
    }

    // Add this method inside the InviteController
    @PostMapping("/rsvp/{inviteCode}")
    public String handleRsvp(@PathVariable String inviteCode, @ModelAttribute Rsvpdto rsvpdto, Principal principal) {
        try {
            Invitation invitation = inviteService.getInvitationByInviteCode(inviteCode);
            if (invitation == null) {
                throw new RuntimeException("Invitation not found");
            }

            if (!invitation.getInviteeEmail().equalsIgnoreCase(principal.getName())) {
                throw new RuntimeException("Unauthorized to RSVP for this invitation");
            }

            if ("accepted".equalsIgnoreCase(rsvpdto.getResponse())) {
                inviteService.acceptInvitationByToken(invitation.getToken(), principal.getName());
            } else {
                inviteService.rejectInvitation(invitation.getId());
            }

            return "redirect:/events/confirmation?status=" + rsvpdto.getResponse();
        } catch (Exception e) {
            return "redirect:/events/confirmation?error=" + e.getMessage();
        }
    }





    // List all invites for the currently logged-in user
    @GetMapping("/my")
    public List<Invitation> getMyInvites(Principal principal) {
        return inviteService.getAcceptedOrPendingInvitationsByInviteeEmail(principal.getName());
    }


}
