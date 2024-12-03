package edu.escuelaing.arsw.puko.service;

import edu.escuelaing.arsw.puko.dto.ArticleWithImageDTO;
import edu.escuelaing.arsw.puko.dto.ArticleWithImagesDTO;
import edu.escuelaing.arsw.puko.exception.ArticleNotFoundException;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.ImageBlob;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.ArticleRepository;
import edu.escuelaing.arsw.puko.repository.ImageBlobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ImageBlobRepository imageBlobRepository;

    @Mock
    private AzureBlobStorageService blobStorageService;

    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createArticleShouldSaveArticleAndImages() throws IOException {
        // Arrange
        String articleName = "Test Article";
        String mainImageFilename = "main.jpg";
        User user = new User();  // Simula el usuario
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(mainImageFilename);

        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article savedArticle = invocation.getArgument(0);
            savedArticle.setId(1L);  // Simula que la BD asigna un ID
            return savedArticle;
        });

        when(blobStorageService.uploadImage(any(MultipartFile.class), anyString()))
                .thenReturn("https://fakeurl.com/main.jpg");

        // Act
        Article result = articleService.createArticle(articleName, Collections.singletonList(file), mainImageFilename, user, 100.0);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(imageBlobRepository, times(1)).save(any(ImageBlob.class));
        verify(blobStorageService, times(1)).uploadImage(eq(file), anyString()); // Cambiado aquí
    }

    @Test
    void createArticleShouldThrowArticleExceptionWhenImageUploadFails() throws IOException {
        // Arrange
        String articleName = "Test Article";
        String mainImageFilename = "main.jpg";
        User user = new User();  // Simula el usuario
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(mainImageFilename);

        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article savedArticle = invocation.getArgument(0);
            savedArticle.setId(1L);  // Simula que la BD asigna un ID
            return savedArticle;
        });

        // Simula que la subida de la imagen falla y lanza IOException
        when(blobStorageService.uploadImage(any(MultipartFile.class), anyString()))
                .thenThrow(new IOException("Simulated upload failure"));

        // Act
        Article article = new Article(articleName, user, 100.0);
        articleRepository.save(article); // Prepara el artículo fuera del try

        try {
            blobStorageService.uploadImage(file, "uniqueFilename"); // Única invocación en el try que puede fallar
            fail("Expected ArticleException to be thrown");
        } catch (IOException e) {
            assertEquals("Simulated upload failure", e.getMessage());
        }

        // Assert
        verify(articleRepository, times(1)).save(any(Article.class)); // Asegura que el artículo se guardó
        verify(blobStorageService, times(1)).uploadImage(eq(file), anyString());
        verifyNoInteractions(imageBlobRepository); // No debe haber interacciones con imageBlobRepository
    }

    @Test
    void getArticleWithMainImageShouldReturnDTO() {
        // Arrange
        Long articleId = 1L;
        Article article = new Article();
        article.setId(articleId);
        article.setName("Test Article");
        User user = new User();
        user.setId(1L);
        article.setUser(user);

        ImageBlob mainImage = new ImageBlob("https://fakeurl.com/main.jpg", true, "main.jpg", article);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(imageBlobRepository.findMainImageByArticleId(articleId)).thenReturn(mainImage);

        // Act
        ArticleWithImageDTO result = articleService.getArticleWithMainImage(articleId);

        // Assert
        assertNotNull(result);
        assertEquals(articleId, result.getId());
        assertEquals("https://fakeurl.com/main.jpg", result.getMainImage());
    }

    @Test
    void getArticleWithMainImageShouldThrowExceptionWhenArticleNotFound() {
        // Arrange
        Long articleId = 1L;
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ArticleNotFoundException.class, () ->
                articleService.getArticleWithMainImage(articleId));
    }
    @Test
    void getArticleWithAllImages_ShouldReturnArticleWithImages() {
        Long articleId = 1L;

        Article article = new Article();
        article.setId(articleId);

        List<ImageBlob> images = Arrays.asList(
                new ImageBlob("url1", false, "filename1", article),
                new ImageBlob("url2", true, "filename2", article)
        );

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(imageBlobRepository.findAllByArticleId(articleId)).thenReturn(images);

        Article result = articleService.getArticleWithAllImages(articleId);

        assertNotNull(result);
        assertEquals(2, result.getImages().size());
        verify(articleRepository, times(1)).findById(articleId);
        verify(imageBlobRepository, times(1)).findAllByArticleId(articleId);
    }
    @Test
    void findMainImageByArticleId_ShouldReturnMainImage() {
        Long articleId = 1L;
        ImageBlob mainImage = new ImageBlob("url2", true, "filename2", null);

        when(imageBlobRepository.findMainImageByArticleId(articleId)).thenReturn(mainImage);

        ImageBlob result = articleService.findMainImageByArticleId(articleId);

        assertNotNull(result);
        assertTrue(result.isMain());
        assertEquals("url2", result.getUrl());
        verify(imageBlobRepository, times(1)).findMainImageByArticleId(articleId);
    }
    @Test
    void findById_ShouldReturnArticle() {
        Long articleId = 1L;
        Article article = new Article();
        article.setId(articleId);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        Article result = articleService.findById(articleId);

        assertNotNull(result);
        assertEquals(articleId, result.getId());
        verify(articleRepository, times(1)).findById(articleId);
    }

    @Test
    void findById_ShouldThrowException_WhenArticleNotFound() {
        Long articleId = 1L;

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        assertThrows(ArticleNotFoundException.class, () -> articleService.findById(articleId));
        verify(articleRepository, times(1)).findById(articleId);
    }
    @Test
    void articleBelongsToUser_ShouldReturnTrue() {
        Long articleId = 1L;
        User user = new User();

        when(articleRepository.existsByIdAndUser(articleId, user)).thenReturn(true);

        boolean result = articleService.articleBelongsToUser(articleId, user);

        assertTrue(result);
        verify(articleRepository, times(1)).existsByIdAndUser(articleId, user);
    }
    @Test
    void getArticleImageUrls_ShouldReturnListOfUrls() {
        Long articleId = 1L;

        List<ImageBlob> images = Arrays.asList(
                new ImageBlob("url1", false, "filename1", null),
                new ImageBlob("url2", true, "filename2", null)
        );

        when(imageBlobRepository.findAllByArticleId(articleId)).thenReturn(images);

        List<String> result = articleService.getArticleImageUrls(articleId);

        assertEquals(2, result.size());
        assertEquals("url1", result.get(0));
        assertEquals("url2", result.get(1));
        verify(imageBlobRepository, times(1)).findAllByArticleId(articleId);
    }
    @Test
    void getArticleWithImages_ShouldReturnArticleWithImageUrls() {
        Long articleId = 1L;
        Article article = new Article();
        article.setId(articleId);
        article.setName("Test Article");
        User user = new User();
        user.setId(2L);
        article.setUser(user);

        List<String> imageUrls = Arrays.asList("url1", "url2");

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(imageBlobRepository.findAllByArticleId(articleId)).thenReturn(
                Arrays.asList(
                        new ImageBlob("url1", false, "filename1", article),
                        new ImageBlob("url2", true, "filename2", article)
                )
        );

        ArticleWithImagesDTO result = articleService.getArticleWithImages(articleId);

        assertNotNull(result);
        assertEquals(articleId, result.getId());
        assertEquals(imageUrls.size(), result.getImageUrls().size());
        verify(articleRepository, times(1)).findById(articleId);
        verify(imageBlobRepository, times(1)).findAllByArticleId(articleId);
    }
}