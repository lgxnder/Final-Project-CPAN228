package com.humber.sleepPlanRepeat.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Represents a single day cell in the calendar view.
// Contains information about the date, whether it belongs to the current month,
// whether it's today, and the events scheduled for that day.

@Data                   // Combines @Getter, @Setter, @ToString, and other common utilities.
@AllArgsConstructor     // Initializes an all-args constructor.
@NoArgsConstructor
@Builder                // Modularizes adding fields to existing objects.
public class CalendarDay {

    private LocalDate date;
    private boolean isCurrentMonth; // Does the CalendarDay date fall on the same month as today?
    private boolean isToday;        // Is the CalendarDay date the same as today?
    private List<Event> events;     // What events happen today?

    // Check if this CalendarDay has any events.
    public boolean hasEvents() {
        return events != null && !events.isEmpty();
    }

    // Counts how many events are present in CalendarDay.
    public int getEventCount() {
        return events != null ? events.size() : 0;
    }
}