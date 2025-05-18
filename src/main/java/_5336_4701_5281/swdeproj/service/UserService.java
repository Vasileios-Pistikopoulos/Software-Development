package _5336_4701_5281.swdeproj.service;
import _5336_4701_5281.swdeproj.model.User;
import _5336_4701_5281.swdeproj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Εναλλακτικά: κάνε bean στο config
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User registerUser(String email, String username, String plainPassword, String role) {
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        String encodedPassword = passwordEncoder.encode(plainPassword);
        User user = new User(email, username, encodedPassword, role);
        return userRepository.save(user);
    }

}
