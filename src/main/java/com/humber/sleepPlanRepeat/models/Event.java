package com.humber.sleepPlanRepeat.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data                   // Combines @Getter, @Setter, @ToString, and other common utilities.
@NoArgsConstructor      // Initializes a default constructor.
@AllArgsConstructor     // Initializes an all-args constructor.
@Builder                // Modularizes adding fields to existing objects.
@Entity                 // Indicates class is a JPA entity.
@Table(name = "events")
public class Event {

    @Id // Initialize primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private String description;

    @Column
    private String externalLink; // users can add a link to maybe a online Zoom meeting, etc

    @Column
    private String focusTag;

    @Column
    private String color;

    @Column(nullable = false)
    private String priority = ""; // Priority level: Low, Medium, High

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



    public Event(String title, LocalDateTime startTime, LocalDateTime endTime, String description, User user) {
        this.title = title;             // Name of event
        this.startTime = startTime;     // Start time of event
        this.endTime = endTime;         // End time of event
        this.description = description; // Description of event
        this.user = user;
    }
}
