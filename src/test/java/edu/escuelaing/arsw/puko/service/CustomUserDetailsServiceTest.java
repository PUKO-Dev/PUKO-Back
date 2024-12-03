package edu.escuelaing.arsw.puko.service;

import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository; // Mock del repositorio

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService; // El servicio a probar
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserExists() {
        // Preparar datos
        String username = "testUser";
        User user = new User("testUser", "password123", "test@example.com");

        // Simular que el repositorio devuelve el usuario cuando se busca por nombre de usuario
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);

        // Llamar al método que estamos probando
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Verificar el comportamiento esperado
        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
    }
    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Preparar datos
        String username = "nonExistentUser";

        // Simular que el repositorio no encuentra al usuario
        Mockito.when(userRepository.findByUsername(username)).thenReturn(null);

        // Llamar al método y verificar que se lanza la excepción esperada
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }
}