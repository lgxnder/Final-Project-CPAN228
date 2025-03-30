package com.humber.sleepPlanRepeat.repositories;

import com.humber.sleepPlanRepeat.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserId(Long userId);

    List<Event> findByUserIsNull();
    // For use in finding events available to all users.
    // Ex. Christmas.
}
