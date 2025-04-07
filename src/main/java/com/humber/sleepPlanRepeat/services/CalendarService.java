package com.humber.sleepPlanRepeat.services;

import com.humber.sleepPlanRepeat.models.CalendarDay;
import com.humber.sleepPlanRepeat.models.Event;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    // Initialize a CalendarDay along with events for a given date.
    public CalendarDay createCalendarDay(LocalDate date, List<Event> allEvents, boolean isCurrentMonth, boolean isToday) {

        // Filter events for given date.
        List<Event> dayEvents = allEvents.stream()
                .filter(event -> {
                    LocalDate eventDate = event.getStartTime().toLocalDate();
                    return eventDate.equals(date);
                })
                .collect(Collectors.toList());

        // Build new object based on filtered data.
        return CalendarDay.builder()
                .date(date)
                .isCurrentMonth(isCurrentMonth)
                .isToday(isToday)
                .events(dayEvents)
                .build();
    }

    // Generate CalendarDays for a given month with events.
    public List<CalendarDay> generateCalendarDays(YearMonth yearMonth, List<Event> events) {
        List<CalendarDay> days = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Get first day of month.
        LocalDate firstOfMonth = yearMonth.atDay(1);

        // Get current day of week.
        // (returns 0-6. 0 -- Monday, 6 -- Sunday)
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        // Fill in first week of month with days from previous month.
        LocalDate prevMonth = firstOfMonth.minusDays(1);
        for (int i = dayOfWeek - 1; i >= 0; i--) {
            LocalDate date = prevMonth.minusDays(i);
            days.add(createCalendarDay(date, events, false, date.equals(today)));
        }

        // Append days of current month
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = yearMonth.atDay(i);
            days.add(createCalendarDay(date, events, true, date.equals(today)));
        }

        // Append days from next month to complete the grid.
        // There are usually 42 "cells" total in a calendar over 6 weeks. We will be using that number here.
        int remainingDays = 42 - days.size();
        LocalDate nextMonth = yearMonth.atEndOfMonth().plusDays(1);
        for (int i = 0; i < remainingDays; i++) {
            LocalDate date = nextMonth.plusDays(i);
            days.add(createCalendarDay(date, events, false, date.equals(today)));
        }

        // Return the completed of days.
        return days;
    }
}