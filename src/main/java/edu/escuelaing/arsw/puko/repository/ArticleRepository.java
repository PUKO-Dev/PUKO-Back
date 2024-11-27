package edu.escuelaing.arsw.puko.repository;

import edu.escuelaing.arsw.puko.model.Article;
import edu.escuelaing.arsw.puko.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT COUNT(a) > 0 FROM Article a WHERE a.id = :articleId AND a.user = :user")
    boolean existsByIdAndUser(@Param("articleId") Long articleId, @Param("user") User user);
}

