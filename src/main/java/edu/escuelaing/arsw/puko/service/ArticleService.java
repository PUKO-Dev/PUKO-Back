package edu.escuelaing.arsw.puko.service;

import edu.escuelaing.arsw.puko.dto.ArticleWithImageDTO;
import edu.escuelaing.arsw.puko.dto.ArticleWithImagesDTO;
import edu.escuelaing.arsw.puko.exception.ArticleNotFoundException;
import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.ImageBlob;
import edu.escuelaing.arsw.puko.model.User;
import edu.escuelaing.arsw.puko.repository.ArticleRepository;
import edu.escuelaing.arsw.puko.repository.ImageBlobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ImageBlobRepository imageRepository;

    @Autowired
    private AzureBlobStorageService blobStorageService;

    @Transactional
    public Article createArticle(String name, List<MultipartFile> images, String mainImageFilename, User user, double initialPrice) {
        Article article = new Article(name, user, initialPrice);
        article = articleRepository.save(article);

        boolean mainImageSet = false;

        for (MultipartFile file : images) {
            try {
                // Genera un nombre de archivo único para evitar conflictos
                String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());

                // Sube la imagen a Azure Blob Storage usando el nombre único y obtiene la URL
                String imageUrl = blobStorageService.uploadImage(file, uniqueFilename);

                boolean isMain = file.getOriginalFilename().equals(mainImageFilename) && !mainImageSet;
                ImageBlob image = new ImageBlob(imageUrl, isMain, uniqueFilename, article);
                imageRepository.save(image);

                if (isMain) {
                    mainImageSet = true;
                }

            } catch (IOException e) {
                throw new RuntimeException("Error al subir la imagen", e);
            }
        }

        return article;
    }


    private String generateUniqueFilename(String originalFilename) {
        String uniqueId = UUID.randomUUID().toString();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return uniqueId + extension;
    }

    public ArticleWithImageDTO getArticleWithMainImage(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId));

        ImageBlob mainImage = imageRepository.findMainImageByArticleId(articleId);

        return new ArticleWithImageDTO(
                article.getId(),
                article.getName(),
                article.getUser().getId(),
                mainImage.getUrl(), // Asigna la imagen en Base64
                article.getInitialPrice()
        );
    }


    public Article getArticleWithAllImages(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId)); // Lanza excepción si no se encuentra
        List<ImageBlob> images = imageRepository.findAllByArticleId(articleId); // Obtiene todas las imágenes del artículo
        article.setImages(images); // Establece todas las imágenes en el artículo
        return article;
    }

    public ImageBlob findMainImageByArticleId(Long articleId) {
        return imageRepository.findMainImageByArticleId(articleId);
    }

    public Article findById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId));
    }

    public boolean articleBelongsToUser(Long articleId, User user) {
        return articleRepository.existsByIdAndUser(articleId, user);
    }

    public List<String> getArticleImageUrls(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId)); // Lanza excepción si no se encuentra

        List<ImageBlob> images = imageRepository.findAllByArticleId(articleId); // Obtiene todas las imágenes del artículo
        List<String> imageUrls = new ArrayList<>();

        for (ImageBlob image : images) {
            imageUrls.add(image.getUrl()); // Utiliza la URL de la imagen
        }

        return imageUrls;
    }

    // Método para obtener el artículo con sus imágenes
    public ArticleWithImagesDTO getArticleWithImages(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId)); // Lanza excepción si no se encuentra

        List<String> imageUrls = getArticleImageUrls(articleId);

        return new ArticleWithImagesDTO(
                article.getId(),
                article.getName(),
                article.getUser().getId(),
                article.getInitialPrice(),
                imageUrls
        );
    }

    public ImageBlob findImageByFilename(Long articleId, String imageId) {
        return imageRepository.findByFilenameAndArticleId(imageId, articleId);
    }
}


