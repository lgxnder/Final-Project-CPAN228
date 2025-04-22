package com.humber.sleepPlanRepeat.services;

        import com.humber.sleepPlanRepeat.models.Event;
        import com.humber.sleepPlanRepeat.models.Invitation;
        import com.humber.sleepPlanRepeat.models.User;
        import com.humber.sleepPlanRepeat.repositories.EventRepository;
        import com.humber.sleepPlanRepeat.repositories.InviteRepository;
        import com.humber.sleepPlanRepeat.repositories.UserRepository;
        import jakarta.transaction.Transactional;
        import java.security.Principal;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Service;

        import java.util.List;
        import java.util.Optional;
        import java.util.UUID;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public InviteService(InviteRepository inviteRepository,
                         UserRepository userRepository,
                         EventRepository eventRepository) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public Invitation createInvitation(Invitation invitation) {
        // Generate a secure token
        String token = UUID.randomUUID().toString();
        invitation.setToken(token);
        return inviteRepository.save(invitation);
    }


    // Find invitations by event ID
    public List<Invitation> getInvitationsByEvent(Long eventId) {
        return inviteRepository.findByEventId(eventId);
    }

    // Find invitation by invitee email and event ID
    public Optional<Invitation> getInvitationByInviteeEmailAndEventId(String inviteeEmail, Long eventId) {
        return inviteRepository.findByInviteeEmailAndEventId(inviteeEmail, eventId);
    }

    // Find invitations by status
    public List<Invitation> getInvitationsByStatus(String status) {
        return inviteRepository.findByStatus(status);
    }

    // Find pending invitations by event ID
    public List<Invitation> getPendingInvitationsByEventId(Long eventId) {
        return inviteRepository.findByEventIdAndStatus(eventId, "PENDING");
    }

    // Find all accepted or pending invitations for a user
    public List<Invitation> getAcceptedOrPendingInvitationsByInviteeEmail(String inviteeEmail) {
        return inviteRepository.findAcceptedOrPendingByInviteeEmail(
                inviteeEmail,
                Invitation.InvitationStatus.PENDING,
                Invitation.InvitationStatus.ACCEPTED
        );
    }

    @Transactional(rollbackOn = Exception.class)
    // Accept an invitation and update its status
    public Invitation acceptInvitation(Long invitationId) {
        Optional<Invitation> invitationOptional = inviteRepository.findById(invitationId);
        if (invitationOptional.isPresent()) {
            Invitation invitation = invitationOptional.get();
            invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
            Invitation savedInvitation = inviteRepository.save(invitation);

            // Get the invitee's User object
            String inviteeEmail = invitation.getInviteeEmail();
            User invitee = userRepository.findByEmail(inviteeEmail)
                    .orElseThrow(() -> new RuntimeException("Invitee not found"));

            // Duplicate the event
            Event original = invitation.getEvent();
            Event sharedCopy = new Event();
            sharedCopy.setTitle(original.getTitle());
            sharedCopy.setStartTime(original.getStartTime());
            sharedCopy.setEndTime(original.getEndTime());
            sharedCopy.setDescription(original.getDescription());
            sharedCopy.setUser(invitee); // assign invitee as owner
            sharedCopy.setOriginalEvent(original); // link back to original
            sharedCopy.setShared(true); // if you use a flag to denote shared copies

            // Save the shared copy
            eventRepository.save(sharedCopy);

            return savedInvitation;
        } else {
            throw new RuntimeException("Invitation not found");
        }
    }

    // Reject an invitation and update its status
    public Invitation rejectInvitation(Long invitationId) {
        Optional<Invitation> invitationOptional = inviteRepository.findById(invitationId);
        if (invitationOptional.isPresent()) {
            Invitation invitation = invitationOptional.get();
            invitation.setStatus(Invitation.InvitationStatus.DECLINED);
            return inviteRepository.save(invitation);
        } else {
            throw new RuntimeException("Invitation not found");
        }
    }

    // Find all invitations for a specific event
    public List<Invitation> getInvitationsByEvent(Event event) {
        return inviteRepository.findByEvent(event);
    }


    // This will let the recipient users of an invite email be able to accept an RSVP email invite only if they have a valid token
    @Transactional
    public Invitation acceptInvitationByToken(String token, String username) {
        Invitation invitation = inviteRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        // Auth check: must be the invitee
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (!invitation.getInviteeEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new RuntimeException("Unauthorized to accept this invitation.");
        }

        if (invitation.getStatus() == Invitation.InvitationStatus.ACCEPTED) {
            throw new RuntimeException("Invitation already accepted.");
        }

        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        Invitation savedInvitation = inviteRepository.save(invitation);

        // Duplicate event to invitee's calendar
        Event original = invitation.getEvent();
        Event sharedCopy = new Event();
        sharedCopy.setTitle(original.getTitle());
        sharedCopy.setStartTime(original.getStartTime());
        sharedCopy.setEndTime(original.getEndTime());
        sharedCopy.setDescription(original.getDescription());
        sharedCopy.setUser(currentUser); // This is the invitee accepting
        sharedCopy.setOriginalEvent(original);
        sharedCopy.setShared(true);
        sharedCopy.setSyncedWithOriginal(true); // mark as in sync

        eventRepository.save(sharedCopy);

        return savedInvitation;
    }

    public Invitation getInvitationByInviteCode(String inviteCode) {
        return inviteRepository.findByToken(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invitation with token not found: " + inviteCode));
    }


}

