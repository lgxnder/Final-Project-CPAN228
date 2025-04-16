package com.humber.sleepPlanRepeat.repositories;
import com.humber.sleepPlanRepeat.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
// Extend interface to allow entities to utilize Create, Read, Update, and Delete (CRUD) operations.
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find events by user ID.
    List<Event> findByUserId(Integer userId);

    // Find global events available to all users.
    List<Event> findByUserIsNull();

    // Find global events within a date range.
    @Query("SELECT e FROM Event e WHERE e.user IS NULL AND e.startTime BETWEEN :start AND :end")
    List<Event> findGlobalEventsByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Find user events within a date range.
    @Query("SELECT e FROM Event e WHERE e.user.id = :userId AND e.startTime BETWEEN :start AND :end")
    List<Event> findByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Find both global and user events within a date range.
    @Query("SELECT e FROM Event e WHERE (e.user.id = :userId OR e.user IS NULL) AND e.startTime BETWEEN :start AND :end")
    List<Event> findAllEventsByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Find events by month.
    @Query("SELECT e FROM Event e WHERE YEAR(e.startTime) = :year AND MONTH(e.startTime) = :month")
    List<Event> findByYearAndMonth(
            @Param("year") int year,
            @Param("month") int month
    );

    // Find user events by month.
    @Query("SELECT e FROM Event e WHERE e.user.id = :userId AND YEAR(e.startTime) = :year AND MONTH(e.startTime) = :month")
    List<Event> findByUserIdAndYearAndMonth(
            @Param("userId") Integer userId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("SELECT e FROM Event e WHERE e.startTime BETWEEN :start AND :end AND e.user IS NOT NULL")
    List<Event> findEventsStartingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
