package com.humber.sleepPlanRepeat.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data                   // Combines @Getter, @Setter, @ToString, and other common utilities.
@NoArgsConstructor      // Initializes a default constructor.
@AllArgsConstructor     // Initializes an all-args constructor.
@Builder                // Modularizes adding fields to existing objects.
@Entity                 // Indicates class is a JPA entity.
@Table(name = "users")
public class User {

    @Id // Initialize primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private boolean enableEmailNotifications = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Event> events;
}
