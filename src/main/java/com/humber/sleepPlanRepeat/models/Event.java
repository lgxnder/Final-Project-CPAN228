package com.humber.sleepPlanRepeat.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {

    @Id
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
    private String externalLink;

    @Column
    private String focusTag;

    @Column
    private String color;

    @Column(nullable = false)
    private String priority = "";

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Points to the original event if this is a shared copy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_event_id")
    private Event originalEvent;

    // List of shared copies of this event
    @Builder.Default
    @OneToMany(mappedBy = "originalEvent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Event> sharedCopies = new ArrayList<>();

    @Column(nullable = false)
    private boolean isShared = false; // will be sued to indicate whether the event is shared or if it is originally created by the currently logged in user.

    @Column(nullable = false)
    private boolean syncedWithOriginal = true;


    // Existing constructor
    public Event(String title, LocalDateTime startTime, LocalDateTime endTime, String description, User user) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.user = user;
    }
}
