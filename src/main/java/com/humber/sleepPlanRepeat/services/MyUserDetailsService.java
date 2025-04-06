package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.ArrayList;


//MyUserDetailsService work in progress
@Service//Used for business logic for the user model
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructor injection
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Override the UserDetailsService interface.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Find user in database by username
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Instantiate new Spring Security UserDetails object for user.
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    new ArrayList<>()
            );

            // ALTERNATIVELY, we could add a role, USER?
            // return org.springframework.security.core.userdetails.User
            //     .withUsername(user.getUsername())
            //     .password(user.getPassword())
            //     .roles("USER")
            //     .build();

        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
