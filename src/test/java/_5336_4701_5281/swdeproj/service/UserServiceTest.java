package _5336_4701_5281.swdeproj.service;

import _5336_4701_5281.swdeproj.model.User;
import _5336_4701_5281.swdeproj.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    void emailExistsReturnsTrueWhenEmailFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));
        assertTrue(userService.emailExists("test@example.com"));
    }

    @Test
    void emailExistsReturnsFalseWhenEmailNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertFalse(userService.emailExists("notfound@example.com"));
    }

    @Test
    void registerUserSuccessfullyCreatesUser() {
        String email = "new@example.com";
        String username = "newuser";
        String password = "password";
        String role = "ROLE_USER";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerUser(email, username, password, role);

        assertEquals(email, user.getEmail());
        assertEquals(username, user.getUsername());
        assertEquals(role, user.getRole());
        assertNotEquals(password, user.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches(password, user.getPassword()));
    }

    @Test
    void registerUserThrowsWhenEmailExists() {
        when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("exists@example.com", "user", "pass", "ROLE_USER"));
    }

    @Test
    void registerUserThrowsWhenUsernameExists() {
        when(userRepository.findByEmail("unique@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("unique@example.com", "existinguser", "pass", "ROLE_USER"));
    }

    @Test
    void getCurrentUserReturnsUserWhenAuthenticated() {
        String username = "user";
        User user = new User();
        user.setUsername(username);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser(authentication);
        assertEquals(user, result);
    }

    @Test
    void getCurrentUserThrowsWhenAuthenticationNull() {
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser(null));
    }

    @Test
    void getCurrentUserThrowsWhenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser(authentication));
    }

    @Test
    void getCurrentUserThrowsWhenUserNotFound() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("missinguser");
        when(userRepository.findByUsername("missinguser")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser(authentication));
    }
}