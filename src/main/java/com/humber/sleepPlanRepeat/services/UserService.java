package com.humber.sleepPlanRepeat.services;
import com.humber.sleepPlanRepeat.models.User;
import com.humber.sleepPlanRepeat.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

//UserService work in progress
@Service//Used for business logic for the user model
public class UserService {

    // Constructor injection
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //save the user to the database
    public int saveUser(User user) {
        //0 - user already exists, 1 - user saved successfully
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return 0;
        }
        //encrypting the password before the save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return 1;
    }
}
