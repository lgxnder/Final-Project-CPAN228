package com.humber.sleepPlanRepeat.repositories;

        import com.humber.sleepPlanRepeat.models.Invitation;
        import com.humber.sleepPlanRepeat.models.Event;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.stereotype.Repository;
        import java.util.List;
        import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invitation, Long> {

    // Find invitations by event ID
    List<Invitation> findByEventId(Long eventId);

    // Find invitation by invitee email and event ID
    Optional<Invitation> findByInviteeEmailAndEventId(String inviteeEmail, Long eventId);

    // Find invitations by invitee email
    List<Invitation> findByInviteeEmail(String inviteeEmail);

    // Find invitations by status
    List<Invitation> findByStatus(String status);

    // Find pending invitations by event ID
    List<Invitation> findByEventIdAndStatus(Long eventId, String status);

    // Find all invitations for a user that are either accepted or pending
    @Query("SELECT i FROM Invitation i WHERE i.inviteeEmail = :inviteeEmail AND i.status IN ('PENDING', 'ACCEPTED')")
    List<Invitation> findAcceptedOrPendingByInviteeEmail(String inviteeEmail);

    // Find invitations by event
    List<Invitation> findByEvent(Event event);
}

