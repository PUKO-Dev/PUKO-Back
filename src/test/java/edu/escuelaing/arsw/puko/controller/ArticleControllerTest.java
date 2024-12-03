package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.dto.ArticleWithImageDTO;
import edu.escuelaing.arsw.puko.dto.ArticleWithImagesDTO;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.ImageBlob;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.ArticleService;
import edu.escuelaing.arsw.puko.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "testuser")
    void testCreateArticle_Success_NoImages() throws Exception {
        User mockUser = new User(); // Crear un mock del usuario
        mockUser.setUsername("testuser");
        mockUser.setId(1L);

        // Simulamos que el servicio encuentra al usuario
        Mockito.when(userService.findByUsername("testuser")).thenReturn(mockUser);

        // Simulamos la creación del artículo
        Article mockArticle = new Article();
        mockArticle.setName("Test Article");
        mockArticle.setId(1L);

        Mockito.when(articleService.createArticle(
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyString(),
                Mockito.any(User.class),
                Mockito.anyDouble()
        )).thenReturn(mockArticle);

        // Realizar la solicitud sin enviar archivos
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/articles")
                        .param("name", "Test Article")
                        .param("mainImageFilename", "mainImage.jpg")
                        .param("initialPrice", "100.0")
                        .with(csrf()))  // Asegurarse de incluir el CSRF si es necesario
                .andExpect(status().isCreated());
    }
    @Test
    @WithMockUser(username = "testuser")
    void testCreateArticle_Unauthorized() throws Exception {
        // Simulamos que el servicio no encuentra el usuario
        Mockito.when(userService.findByUsername("testuser")).thenReturn(null);

        mockMvc.perform(multipart("/api/articles")
                        .param("name", "Test Article")
                        .param("mainImageFilename", "mainImage.jpg")
                        .param("initialPrice", "100.0")
                        .with(csrf())
                        )
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(username = "testuser")
    void testCreateArticle_BadRequest() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setId(1L);

        // Simulamos que el servicio lanza una excepción
        Mockito.when(userService.findByUsername("testuser")).thenReturn(mockUser);
        Mockito.when(articleService.createArticle(
                Mockito.anyString(),
                Mockito.anyList(),
                Mockito.anyString(),
                Mockito.any(User.class),
                Mockito.anyDouble()
        )).thenThrow(new RuntimeException("Creation failed"));

        mockMvc.perform(multipart("/api/articles")
                        .param("name", "Test Article")
                        .param("mainImageFilename", "mainImage.jpg")
                        .param("initialPrice", "100.0")
                        .with(csrf())
                        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetMainImage_Found() throws Exception {
        // Simulamos que el artículo tiene una imagen principal
        ImageBlob mockImage = new ImageBlob();
        mockImage.setUrl("http://example.com/image.jpg");

        Mockito.when(articleService.findMainImageByArticleId(1L)).thenReturn(mockImage);

        // Realizamos la solicitud
        MvcResult result = mockMvc.perform(get("/api/articles/1/main-image"))
                .andExpect(status().isOk())  // Verificar que la respuesta es 200 OK
                .andReturn();  // Capturamos el resultado de la solicitud

        // Capturamos el cuerpo de la respuesta como String
        String responseBody = result.getResponse().getContentAsString();

        // Comparamos el cuerpo de la respuesta con la URL esperada
        assertEquals("http://example.com/image.jpg", responseBody);
    }
    @Test
    @WithMockUser(username = "testuser")
    void testGetMainImage_NotFound() throws Exception {
        Mockito.when(articleService.findMainImageByArticleId(1L)).thenReturn(null);

        mockMvc.perform(get("/api/articles/1/main-image"))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "testuser")
    void testGetMainImage_InternalServerError() throws Exception {
        Mockito.when(articleService.findMainImageByArticleId(1L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/articles/1/main-image"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    @WithMockUser(username = "testuser")
    void testGetArticleWithMainImage_Found() throws Exception {
        // Simulamos que el artículo tiene una imagen principal
        ArticleWithImageDTO mockArticle = new ArticleWithImageDTO();
        mockArticle.setName("Test Article");
        mockArticle.setMainImage("http://example.com/mainImage.jpg");

        // Simulamos la respuesta del servicio
        Mockito.when(articleService.getArticleWithMainImage(1L)).thenReturn(mockArticle);

        // Realizamos la solicitud y capturamos la respuesta
        MvcResult result = mockMvc.perform(get("/api/articles/1/with-image"))
                .andExpect(status().isOk())  // Verificar que la respuesta es 200 OK
                .andReturn();  // Capturamos el resultado de la solicitud

        // Capturamos el cuerpo de la respuesta como String
        String responseBody = result.getResponse().getContentAsString();

        // Comprobamos que la respuesta contiene el valor esperado en el JSON
        assertTrue(responseBody.contains("\"name\":\"Test Article\""));
        assertTrue(responseBody.contains("\"mainImage\":\"http://example.com/mainImage.jpg\""));
    }
    @Test
    @WithMockUser(username = "testuser")
    void testGetArticleWithMainImage_NotFound() throws Exception {
        Mockito.when(articleService.getArticleWithMainImage(1L)).thenThrow(new RuntimeException("Article not found"));

        mockMvc.perform(get("/api/articles/1/with-image"))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "testuser")
    void testGetArticleImages_Found() throws Exception {
        ArticleWithImagesDTO mockImages = new ArticleWithImagesDTO();
        mockImages.setImageUrls(List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"));

        Mockito.when(articleService.getArticleWithImages(1L)).thenReturn(mockImages);
        MvcResult result = mockMvc.perform(get("/api/articles/1/images"))
                .andExpect(status().isOk())  // Verificar que la respuesta es 200 OK
                .andReturn();  // Capturamos el resultado de la solicitud

        // Capturamos el cuerpo de la respuesta como String
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("http://example.com/image1.jpg"), "La respuesta debe contener la primera imagen");
        assertTrue(responseBody.contains("http://example.com/image2.jpg"), "La respuesta debe contener la segunda imagen");
    }

}