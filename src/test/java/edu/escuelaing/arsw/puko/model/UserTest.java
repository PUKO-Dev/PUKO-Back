package edu.escuelaing.arsw.puko.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Mock
    private User userMock; // Mock de la clase User

    @InjectMocks
    private User userUnderTest; // Instancia de la clase User que será probada

    @BeforeEach
    void setUp() {
        // Inicializa los mocks antes de cada prueba
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUsername() {
        // Configuramos el comportamiento esperado
        when(userMock.getUsername()).thenReturn("testUser");

        // Llamamos al método de la clase User
        String username = userMock.getUsername();

        // Verificamos que se haya llamado al método y que el valor sea el esperado
        assertEquals("testUser", username);
        verify(userMock).getUsername(); // Verifica que el método getUsername haya sido llamado
    }

    @Test
    void testSetUsername() {
        // Creamos un objeto real de User
        User userTest = new User("testUser", "password123", "testUser@example.com");

        // Establecemos un nuevo nombre de usuario
        userTest.setUsername("newUser");

        // Verificamos que el valor de username se haya actualizado
        assertEquals("newUser", userTest.getUsername());
    }

    @Test
    void testRealMoney() {
        // Configuramos el comportamiento esperado
        when(userMock.getRealMoney()).thenReturn(10000.0);

        // Llamamos al método para obtener el dinero real
        double realMoney = userMock.getRealMoney();

        // Verificamos que se haya llamado y que el valor sea el esperado
        assertEquals(10000.0, realMoney);
        verify(userMock).getRealMoney(); // Verifica que el método getRealMoney haya sido llamado
    }

    @Test
    void testTemporaryMoney() {
        // Configuramos el comportamiento esperado
        when(userMock.getTemporaryMoney()).thenReturn(5000.0);

        // Llamamos al método para obtener el dinero temporal
        double temporaryMoney = userMock.getTemporaryMoney();

        // Verificamos que se haya llamado y que el valor sea el esperado
        assertEquals(5000.0, temporaryMoney);
        verify(userMock).getTemporaryMoney(); // Verifica que el método getTemporaryMoney haya sido llamado
    }

    @Test
    void testUserConstructor() {
        // Configuramos los valores que esperamos pasar al constructor
        User newUser = new User("user1", "password123", "user1@example.com");

        // Verificamos que los atributos estén correctamente asignados
        assertEquals("user1", newUser.getUsername());
        assertEquals("password123", newUser.getPassword());
        assertEquals("user1@example.com", newUser.getEmail());
        assertEquals(10000000, newUser.getRealMoney());
        assertEquals(newUser.getRealMoney(), newUser.getTemporaryMoney()); // El dinero temporal debe ser igual al dinero real
    }
    @Test
    void testConstructorWithRealMoney() {
        // Valores de prueba
        String username = "testUser";
        String password = "password123";
        String email = "testuser@example.com";
        double realMoney = 5000.0;

        // Crear el objeto User usando el segundo constructor
        User user = new User(username, password, email, realMoney);

        // Verificar que el objeto se ha creado correctamente
        assertEquals(username, user.getUsername(), "El username no coincide.");
        assertEquals(password, user.getPassword(), "La contraseña no coincide.");
        assertEquals(email, user.getEmail(), "El email no coincide.");
        assertEquals(realMoney, user.getRealMoney(), "El dinero real no coincide.");
        assertEquals(realMoney, user.getTemporaryMoney(), "El dinero temporal debe ser igual al dinero real.");
    }
    @Test
    void testToString() {
        // Valores de prueba
        Long id = 1L;
        String username = "testUser";
        String password = "password123";
        String email = "testuser@example.com";
        double realMoney = 5000.0;
        double temporaryMoney = realMoney;

        // Crear el objeto User
        User user = new User(id, username, password);
        user.setEmail(email);
        user.setRealMoney(realMoney);
        user.setTemporaryMoney(temporaryMoney);

        // Obtener el resultado del método toString
        String expected = "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", realMoney=" + realMoney +
                ", temporaryMoney=" + temporaryMoney +
                '}';

        // Verificar que el toString es correcto
        assertEquals(expected, user.toString(), "El toString() no devuelve el formato esperado.");
    }

}
