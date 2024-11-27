package edu.escuelaing.arsw.puko.exception;

public class ArticleNotFoundException extends RuntimeException {
    public ArticleNotFoundException(Long articleId) {
        super("Article not found with ID: " + articleId);
    }

    public ArticleNotFoundException(String message) {
        super(message);
    }
}
