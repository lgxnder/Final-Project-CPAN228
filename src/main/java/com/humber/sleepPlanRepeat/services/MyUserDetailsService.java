package com.humber.sleepPlanRepeat.services;

import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service//Used for business logic for the user model
public class MyUserDetailsService {

    private final UserRepository userRepository;

    //Assigning userrepository for database purposes
    public MyUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }
/*
    //Provide the user info to Spring Security for authentication and authorization
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Finding user in the database username and password if it exists or not
        Optional<User> userOp = userRepository.findByUsername(username);
        if (userOp.isPresent()) {
            User user = userOp.get();
            return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found!");
        }
    }*/
}
