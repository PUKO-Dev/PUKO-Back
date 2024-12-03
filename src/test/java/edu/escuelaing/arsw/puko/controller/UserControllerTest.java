package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Prueba para el endpoint POST /api/users
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DirtiesContext
    void testCreateUser() throws Exception {
        // Configuración de la prueba sin CSRF
        mockMvc.perform(post("/api/users")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("email", "testuser@example.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())) // Asegúrate de que CSRF esté presente solo si es necesario
                .andExpect(status().isCreated());
    }



    @Test
    @WithMockUser
    void testGetUserById() throws Exception {
        User user = new User( 1L,"testuser", "password123");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    // Prueba para el endpoint GET /api/users
    @Test
    @WithMockUser
    void testGetAllUsers() throws Exception {
        User user1 = new User( "testuser1", "password123", "testuser1@example.com");
        User user2 = new User( "testuser2", "password123", "testuser2@example.com");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser1"))
                .andExpect(jsonPath("$[1].username").value("testuser2"));
    }


    @Test
    @WithMockUser
    void testDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1")
                        .with(csrf())) // Asegura que el CSRF esté presente
                .andExpect(status().isNoContent()); // Espera un 204 No Content en la respuesta
    }

    // Prueba para el endpoint GET /api/users/me
    @Test
    @WithMockUser(username = "testuser")
    void testGetUserByUsername() throws Exception {
        User user = new User( "testuser", "password123", "testuser@example.com");

        when(userService.getUserByUsername("testuser")).thenReturn(user);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    @Test
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        // Simulamos que el servicio lanza una excepción
        Mockito.when(userService.getUserById(1L)).thenThrow(new RuntimeException("User not found"));

        // Realizamos la solicitud y verificamos que la respuesta sea 404
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }
}