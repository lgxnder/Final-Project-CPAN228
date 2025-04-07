package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.ArrayList;

@Service
// User model business logic.
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

            // User could not be found in database.
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
