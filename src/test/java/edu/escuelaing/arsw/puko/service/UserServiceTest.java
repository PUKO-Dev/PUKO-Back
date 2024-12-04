package edu.escuelaing.arsw.puko.service;

import edu.escuelaing.arsw.puko.exception.UserNotFoundException;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_ShouldSaveUserWithEncryptedPassword() {
        String username = "testUser";
        String rawPassword = "password";
        String email = "test@example.com";
        String encryptedPassword = "encryptedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encryptedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(username, rawPassword, email);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(encryptedPassword, result.getPassword());
        assertEquals(email, result.getEmail());
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }
    @Test
    void getUserById_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User("testUser", "password", "test@example.com");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }
    @Test
    void getUserByUsername_ShouldReturnUser() {
        String username = "testUser";
        User user = new User(username, "password", "test@example.com");

        when(userRepository.findByEmail(username)).thenReturn(user);

        User result = userService.findByEmail(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByEmail(username);
    }
    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        List<User> users = Arrays.asList(
                new User("user1", "password1", "user1@example.com"),
                new User("user2", "password2", "user2@example.com")
        );

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }
    @Test
    void deleteUser_ShouldCallRepositoryDeleteById() {
        Long userId = 1L;

        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
    @Test
    void checkPassword_ShouldReturnTrue_WhenPasswordsMatch() {
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = userService.checkPassword(rawPassword, encodedPassword);

        assertTrue(result);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    void checkPassword_ShouldReturnFalse_WhenPasswordsDoNotMatch() {
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = userService.checkPassword(rawPassword, encodedPassword);

        assertFalse(result);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }
    @Test
    void findByID_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User("testUser", "password", "test@example.com");
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.findByID(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findByID_ShouldReturnNull_WhenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User result = userService.findByID(userId);

        assertNull(result);
        verify(userRepository, times(1)).findById(userId);
    }
}
