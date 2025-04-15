package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
// User model used in business logic.
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor injection
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Save the user to the database.
    // Returns an integer (0 -- User already exists, 1 -- User saved successfully)
    public int saveUser(User user) {

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return 0;
        }

        // Encrypt password before saving to repository.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return 1;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
