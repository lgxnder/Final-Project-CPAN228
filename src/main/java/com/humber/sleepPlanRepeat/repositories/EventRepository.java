package com.humber.sleepPlanRepeat.repositories;

import com.humber.sleepPlanRepeat.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    // Implement custom methods here.
    // **Filter methods like "findByIgnoreCaseCategoryAndPrice".
    // **Native queries to perform on data in the repository/database.
}
