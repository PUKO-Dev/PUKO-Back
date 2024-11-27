package edu.escuelaing.arsw.puko.repository;

import edu.escuelaing.arsw.puko.model.ImageBlob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageBlobRepository extends JpaRepository<ImageBlob, Long> {

    // Consulta para obtener la imagen principal del artículo usando el campo isMain
    @Query("SELECT i FROM ImageBlob i WHERE i.article.id = :articleId AND i.isMain = true")
    ImageBlob findMainImageByArticleId(@Param("articleId") Long articleId);

    // Método para obtener todas las imágenes de un artículo por su ID
    List<ImageBlob> findAllByArticleId(Long articleId);

    // Consulta para obtener una imagen específica por su nombre de archivo y el ID del artículo
    @Query("SELECT i FROM ImageBlob i WHERE i.filename = :filename AND i.article.id = :articleId")
    ImageBlob findByFilenameAndArticleId(@Param("filename") String filename, @Param("articleId") Long articleId);
}
