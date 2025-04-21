package com.humber.sleepPlanRepeat.repositories;

import com.humber.sleepPlanRepeat.models.Event;
import com.humber.sleepPlanRepeat.models.EventAttendee;
import com.humber.sleepPlanRepeat.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {
    List<EventAttendee> findByEvent(Event event);
    boolean existsByEventAndUser(Event event, User user);
    EventAttendee findByEventAndUser(Event event, User user);
}
