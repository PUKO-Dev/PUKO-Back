package edu.escuelaing.arsw.puko.controller;

import edu.escuelaing.arsw.puko.dto.ArticleWithImageDTO;
import edu.escuelaing.arsw.puko.dto.ArticleWithImagesDTO;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.ImageBlob;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.service.ArticleService;
import edu.escuelaing.arsw.puko.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Article> createArticle(
            @RequestParam String name,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("mainImageFilename") String mainImageFilename,
            @RequestParam double initialPrice,
            @AuthenticationPrincipal UserDetails userDetails) { // Get user ID from the request

        // Fetch user by username
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            // Create the article and associate it with the user
            Article createdArticle = articleService.createArticle(name, images, mainImageFilename, user, initialPrice);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);

        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null); // Provide null body to signify a bad request
        }
    }


    @GetMapping("/{articleId}/main-image")
    public ResponseEntity<String> getMainImage(
            @PathVariable Long articleId) {

        try {
            // Obtener la imagen principal del artículo
            ImageBlob mainImage = articleService.findMainImageByArticleId(articleId);
            if (mainImage != null) {
                return ResponseEntity.status(HttpStatus.OK).body(mainImage.getUrl());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // TODO: Cambiar a en vez de mandar la imagen mandamos la url de la imagen (madurez REST)
    @GetMapping("/{articleId}/with-image")
    public ResponseEntity<ArticleWithImageDTO> getArticleWithMainImage(
            @PathVariable Long articleId) {
        try {
            ArticleWithImageDTO articleWithImage = articleService.getArticleWithMainImage(articleId);
            return ResponseEntity.ok(articleWithImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{articleId}/images")
    public ArticleWithImagesDTO getArticleImages(@PathVariable Long articleId) {
        return articleService.getArticleWithImages(articleId);
    }


}
