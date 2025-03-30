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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNum;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Event> events;

    public User(String firstName, String lastName, String emailAddress, String phoneNum) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.phoneNum = phoneNum;

    }
}
