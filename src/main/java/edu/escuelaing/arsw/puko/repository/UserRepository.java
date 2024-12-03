package edu.escuelaing.arsw.puko.repository;

import edu.escuelaing.arsw.puko.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    // You can define custom queries here if needed
    @Query("SELECT u FROM User u WHERE u.username = :username")
    User findByUsername(String username);

    User findByEmail(String email);
}
