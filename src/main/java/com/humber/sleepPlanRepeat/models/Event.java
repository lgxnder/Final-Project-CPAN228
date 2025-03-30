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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
