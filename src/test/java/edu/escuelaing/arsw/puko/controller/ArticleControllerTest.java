package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.dto.ArticleWithImageDTO;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.ImageBlob;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.ArticleService;
import edu.escuelaing.arsw.puko.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArticleControllerTest {

    @InjectMocks
    private ArticleController articleController;

    @Mock
    private ArticleService articleService;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateArticle_Success() {
        // Mock inputs
        String name = "Test Article";
        List<MultipartFile> images = new ArrayList<>();
        String mainImageFilename = "mainImage.jpg";
        double initialPrice = 100.0;

        User user = new User();
        user.setUsername("testUser");
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userService.findByEmail("testUser")).thenReturn(user);

        Article createdArticle = new Article();
        createdArticle.setName(name);
        when(articleService.createArticle(name, images, mainImageFilename, user, initialPrice)).thenReturn(createdArticle);

        // Call the method
        ResponseEntity<Article> response = articleController.createArticle(name, images, mainImageFilename, initialPrice, userDetails);

        // Verify results
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        assertEquals(createdArticle, response.getBody());
        verify(userService, times(1)).findByEmail("testUser");
        verify(articleService, times(1)).createArticle(name, images, mainImageFilename, user, initialPrice);
    }
    @Test
    void testCreateArticle_UnauthorizedUser() {
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userService.findByEmail("testUser")).thenReturn(null);

        ResponseEntity<Article> response = articleController.createArticle("Test Article", new ArrayList<>(), "mainImage.jpg", 100.0, userDetails);

        assertEquals(401, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userService, times(1)).findByEmail("testUser");
        verify(articleService, never()).createArticle(anyString(), anyList(), anyString(), any(User.class), anyDouble());
    }

    @Test
    void testGetMainImage_Success() {
        Long articleId = 1L;
        ImageBlob imageBlob = new ImageBlob();
        imageBlob.setUrl("http://example.com/mainImage.jpg");

        when(articleService.findMainImageByArticleId(articleId)).thenReturn(imageBlob);

        ResponseEntity<String> response = articleController.getMainImage(articleId);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        assertEquals("http://example.com/mainImage.jpg", response.getBody());
        verify(articleService, times(1)).findMainImageByArticleId(articleId);
    }

    @Test
    void testGetMainImage_NotFound() {
        Long articleId = 1L;
        when(articleService.findMainImageByArticleId(articleId)).thenReturn(null);

        ResponseEntity<String> response = articleController.getMainImage(articleId);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNull(response.getBody());
        verify(articleService, times(1)).findMainImageByArticleId(articleId);
    }

    @Test
    void testGetArticleWithMainImage_Success() {
        Long articleId = 1L;
        ArticleWithImageDTO articleWithImageDTO = new ArticleWithImageDTO();
        when(articleService.getArticleWithMainImage(articleId)).thenReturn(articleWithImageDTO);

        ResponseEntity<String> response = articleController.getArticleWithMainImage(articleId);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        verify(articleService, times(1)).getArticleWithMainImage(articleId);
    }


}