package com.humber.sleepPlanRepeat.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data                   // Combines @Getter, @Setter, @ToString, and other common utilities.
@NoArgsConstructor      // Initializes a default constructor.
@AllArgsConstructor     // Initializes an all-args constructor.
@Builder                // Modularizes adding fields to existing objects.
@Entity                 // Indicates class is a JPA entity.
@Table(name = "events")
public class Event {

    @Id // Initialize primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // See about utilizing 1 or 2 libraries in this class alone for timekeeping.
    // https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html
    // https://www.w3schools.com/java/java_date.asp
    private String datetime;
    // Date & time information.
    // See about implementation
    // -- consider other "datetime" formats such as,
    // yyyy-mm-dd-24hh-mm-ss,
    // yyyy-mm-dd-12hh-am/pm-mm-ss,
    // dd-mm-yyyy-12hh-am/pm-mm-ss, or
    // dd-mm-yyyy-12hh-am/pm-mm.

    private String label;
    private String description;
    // A purely visual description for Event set by a USER.
    // To be displayed on front-end in schedule-view.

    // Could possibly add:
    //      private String colour;
    // See about implementation after front-end is finished.
    // Ensure Canadian spelling of "colour".

    // **NEED TO PROPERLY IMPLEMENT DATETIME.
    public Event(String datetime, String label, String description) {
        this.datetime = datetime;
        this.label = label;
        this.description = description;
    }
}
