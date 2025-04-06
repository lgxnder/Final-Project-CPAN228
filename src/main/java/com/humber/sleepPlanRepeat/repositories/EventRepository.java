package com.humber.sleepPlanRepeat.repositories;

import com.humber.sleepPlanRepeat.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserId(Integer userId);
    List<Event> findByUserIsNull();
    // For use in finding events available to all users.
    // Ex. Christmas.

    @Query("SELECT e FROM Event e WHERE e.user IS NULL AND e.startTime BETWEEN :start AND :end")
    List<Event> findGlobalEventsByDateRange(LocalDateTime start, LocalDateTime end);
}
