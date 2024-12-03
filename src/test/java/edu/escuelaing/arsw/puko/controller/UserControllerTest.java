package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.controller.UserController;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    public void testCreateUserSuccess() {
        // Mock de datos de entrada y resultado esperado
        String username = "testUser";
        String password = "password123";
        String email = "test@example.com";

        User mockUser = new User(username, email, password);

        when(userService.createUser(username, password, email)).thenReturn(mockUser);

        // Ejecutar la prueba
        ResponseEntity<User> response = userController.createUser(username, password, email);

        // Validaciones
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(mockUser.getId(), response.getBody().getId());
    }

    @Test
    public void testGetUserByIdSuccess() {
        // Mock de datos de entrada y resultado esperado
        Long userId = 1L;
        User mockUser = new User( "testUser", "test@example.com", "password123");

        when(userService.getUserById(userId)).thenReturn(mockUser);

        // Ejecutar la prueba
        ResponseEntity<User> response = userController.getUserById(userId);

        // Validaciones
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(mockUser.getId(), response.getBody().getId());
    }

    @Test
    public void testGetUserByIdNotFound() {
        // Mock para simular que el usuario no existe
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new RuntimeException("User not found"));

        // Ejecutar la prueba
        ResponseEntity<User> response = userController.getUserById(userId);

        // Validaciones
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void testGetAllUsersSuccess() {
        // Mock de lista de usuarios
        User mockUser1 = new User( "user1", "user1@example.com", "password1");
        User mockUser2 = new User( "user2", "user2@example.com", "password2");

        when(userService.getAllUsers()).thenReturn(List.of(mockUser1, mockUser2));

        // Ejecutar la prueba
        ResponseEntity<List<User>> response = userController.getAllUsers();

        // Validaciones
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testDeleteUserSuccess() {
        // Mock de ID del usuario
        Long userId = 1L;

        doNothing().when(userService).deleteUser(userId);

        // Ejecutar la prueba
        ResponseEntity<Void> response = userController.deleteUser(userId);

        // Validaciones
        assertEquals(204, response.getStatusCodeValue());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    public void testGetUserByUsernameSuccess() {
        // Mock de UserDetails y User
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        User mockUser = new User( "testUser", "test@example.com", "password123");

        when(userService.getUserByEmail("test@example.com")).thenReturn(mockUser);

        // Ejecutar la prueba
        ResponseEntity<User> response = userController.getUserByUsername(userDetails);

        // Validaciones
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(mockUser.getId(), response.getBody().getId());
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        // Mock de UserDetails y servicio
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("nonexistent@example.com");

        when(userService.getUserByEmail("nonexistent@example.com")).thenThrow(new RuntimeException("User not found"));

        // Ejecutar la prueba
        ResponseEntity<User> response = userController.getUserByUsername(userDetails);

        // Validaciones
        assertEquals(404, response.getStatusCodeValue());
    }
}
