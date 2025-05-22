package _5336_4701_5281.swdeproj.repository;
import _5336_4701_5281.swdeproj.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByRolesContaining(User.Role role);
}