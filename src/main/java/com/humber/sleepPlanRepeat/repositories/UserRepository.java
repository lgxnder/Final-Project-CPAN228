package com.humber.sleepPlanRepeat.repositories;
import com.humber.sleepPlanRepeat.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
// Extend interface to allow entities to utilize Create, Read, Update, and Delete (CRUD) operations.
public interface UserRepository extends JpaRepository<User, Integer> {

    // Find a user by their username.
    public Optional<User> findByUsername(String username);
}
