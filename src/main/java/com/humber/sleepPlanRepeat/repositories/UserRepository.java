package com.humber.sleepPlanRepeat.repositories;
import com.humber.sleepPlanRepeat.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

//Referring this interface as the repository
@Repository
//JPARepository is used as an interface for entities to used CRUD
public interface UserRepository extends JpaRepository<User, Long> {
    //find the user by the username
    public Optional<User> findByUsername(String username);
}
